package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sap.fontus.Constants.TAINT_PREFIX;

public class StatementTainter extends StatementVisitorAdapter {

	private final List<Taint> taints;
	private final AssignmentInfos assignmentInfos;
	private List<AssignmentValue> columnValues = new ArrayList<>();
	private final List<Integer> parameterIndices;

	public StatementTainter(List<Taint> taints) {
		this.taints = taints;
		this.assignmentInfos = new AssignmentInfos();
		this.parameterIndices = new ArrayList<>();
	}

	public Map<String, String> getAssignmentInfos() {
		return this.assignmentInfos.getAssignmentInfosAsString();
	}

	@Override
	public void visit(Statements stmts) {
		for (Statement statement : stmts.getStatements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(Select select) {
		SelectTainter selectTainter = new SelectTainter(this.taints);
		selectTainter.setAssignmentValues(this.columnValues);
		select.getSelectBody().accept(selectTainter);
		if (select.getWithItemsList() != null)
			for (WithItem withItem : select.getWithItemsList()) {
				SelectTainter innerSelectTainter = new SelectTainter(this.taints);
				innerSelectTainter.setAssignmentValues(this.columnValues);
				withItem.accept(innerSelectTainter);
			}
	}

	@Override
	public void visit(Insert insert) {
		this.columnValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();
		if (insert.getColumns() != null) {
			insert.setColumns(this.taintColumns(insert.getColumns()));
			for (Column c : insert.getColumns()) {
				columnNames.add(c.getColumnName());
			}
		}
		this.addTemporaryAssingVariables(columnNames);
		if (insert.getItemsList() != null) {
			ItemsListTainter itemListTainter = new ItemsListTainter(this.taints);
			itemListTainter.setAssignmentValues(this.columnValues);
			// Loop after setColumns will also include new taint columns if needed
			insert.getItemsList().accept(itemListTainter);
		}
		if (insert.getSelect() != null)
			insert.getSelect().accept(this);
		if (insert.getReturningExpressionList() != null)
			insert.setReturningExpressionList(this.taintReturningExpression(insert.getReturningExpressionList()));
		this.addTemporaryAssignValues(this.columnValues);
		//System.out.println("Insert");
		//assignmentInfos.getAssignmentInfosAsString().forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
	}

	@Override
	public void visit(Update update) {
		this.columnValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();
		// Parser breaks if !update.isUseColumnBrackets() and unrecognized
		// update.isUseSelect()
		// for example: update a set id = (select id from b);

		List<UpdateSet> updateSets = update.getUpdateSets();
		List<Column> taintedCols = new ArrayList<>();
		List<Expression> taintedExprs = new ArrayList<>();
		List<UpdateSet> fixedSets = new ArrayList<>();
		//System.out.println(update);
		for(UpdateSet updateSet : updateSets) {

			List<Column> columns = updateSet.getColumns();
			ArrayList<Column> tcols = (ArrayList<Column>) this.taintColumns(columns);
			List<Expression> expressions = updateSet.getExpressions();
			ArrayList<Expression> texprs = (ArrayList<Expression>) this.taintExpressions(expressions);
			// This seems to be relevant for subselects, not 100% sure why/how
			if (texprs.get(0) instanceof SubSelect) {
				UpdateSet us = new UpdateSet();
				us.setColumns(tcols);
				us.setExpressions(texprs);
				us.setUsingBracketsForColumns(true);
				fixedSets.add(us);
			} else {
				for (int i = 0; i < tcols.size(); i++) {
					Column c = tcols.get(i);
					columnNames.add(c.getColumnName());
					Expression e = texprs.get(i);
					UpdateSet us = new UpdateSet(c, e);
					//System.out.println(c + " = " + e);

					if (i == 0 && e instanceof SubSelect) {
						us.setColumns(tcols);
						us.setExpressions(texprs);
						fixedSets.add(us);
						break;
					}
					fixedSets.add(us);
				}
			}
		}
		updateSets.clear();
		updateSets.addAll(fixedSets);


		this.addTemporaryAssingVariables(columnNames);
		this.addTemporaryAssignValues(this.columnValues);
		//System.out.println("Update");
		//assignmentInfos.getAssignmentInfosAsString().forEach((k,v) -> System.out.println("key: "+k+" value:"+v));

		/*if (update.getColumns() != null)
			update.setColumns(taintColumns(update.getColumns()));
		*/
		/*if (!update.isUseSelect()) {
			// check needed to avoid a duplicated select as expression
			if (update.getExpressions() != null)
				update.setExpressions(taintExpressions(update.getExpressions()));
		} else {
			if (update.getSelect() != null)
				update.getSelect().accept(this);
		}*/

		if (update.getFromItem() != null)
			update.getFromItem().accept(new FromItemTainter(this.taints));

	}

	@Override
	public void visit(Replace replace) {
		if (replace.getColumns() != null) {
			replace.setColumns(this.taintColumns(replace.getColumns()));
		}
		if (replace.getExpressions() != null) {
			replace.setExpressions(this.taintExpressions(replace.getExpressions()));
		}
		if (replace.getItemsList() != null) {
			ItemsListTainter itemsListTainter = new ItemsListTainter(this.taints);
			replace.getItemsList().accept(itemsListTainter);
		}
	}

	@Override
	public void visit(Merge merge) {
		if (merge.getMergeInsert() != null) {
			merge.getMergeInsert().setColumns(this.taintColumns(merge.getMergeInsert().getColumns()));
			merge.getMergeInsert().setValues(this.taintExpressions(merge.getMergeInsert().getValues()));
		}
		if (merge.getMergeUpdate() != null) {
			merge.getMergeUpdate().setColumns(this.taintColumns(merge.getMergeUpdate().getColumns()));
			merge.getMergeUpdate().setValues(this.taintExpressions(merge.getMergeUpdate().getValues()));
		}
		if (merge.getUsingSelect() != null) {
			merge.getUsingSelect().accept(new ItemsListTainter(this.taints));
		}
	}

	@Override
	public void visit(Execute execute) {
		if (execute.getExprList() != null)
			execute.getExprList().accept(new ItemsListTainter(this.taints));
	}

	private List<Expression> taintExpressions(List<Expression> expressions) {
		List<Expression> newExpressions = new ArrayList<>();
		List<Expression> expressionReference = new ArrayList<>(1);
		ExpressionTainter expressionTainter = new ExpressionTainter(this.taints, expressionReference);
		expressionTainter.setAssignmentValues(this.columnValues);
		for (Expression expression : expressions) {
			newExpressions.add(expression);
			expression.accept(expressionTainter);
			if (!expressionReference.isEmpty()) {
				newExpressions.add(expressionReference.get(0));
				expressionReference.clear();
			}
		}
		return newExpressions;
	}

	private List<SelectExpressionItem> taintReturningExpression(List<SelectExpressionItem> returningExpressionList) {
		List<SelectExpressionItem> newReturningExpressionList = new ArrayList<>();
		List<SelectItem> selectItemReference = new ArrayList<>(1);
		SelectItemTainter selectItemTainter = new SelectItemTainter(this.taints, selectItemReference);
		for (SelectExpressionItem selectExpressionItem : returningExpressionList) {
			newReturningExpressionList.add(selectExpressionItem);
			selectExpressionItem.accept(selectItemTainter);
			if (!selectItemReference.isEmpty()) {
				newReturningExpressionList.add((SelectExpressionItem) selectItemReference.get(0));
				selectItemReference.clear();
			}
		}
		return newReturningExpressionList;
	}

	private List<Column> taintColumns(List<Column> columns) {
		List<Column> newColumns = new ArrayList<>();
		List<Expression> expressionReference = new ArrayList<>(1);
		ExpressionTainter expressionTainter = new ExpressionTainter(this.taints, expressionReference);
		for (Column column : columns) {
			newColumns.add(column);
			column.accept(expressionTainter);
			if (!expressionReference.isEmpty()) {
				newColumns.add((Column) expressionReference.get(0));
				expressionReference.clear();
			}
		}
		return (newColumns);
	}

	@Override
	public void visit(CreateTable createTable) {
		if (createTable.getColumnDefinitions() != null)
			createTable.setColumnDefinitions(this.taintColumnDefinitions(createTable.getColumnDefinitions()));
		if (createTable.getSelect() != null)
			createTable.getSelect().accept(this);			
	}

	private List<ColumnDefinition> taintColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
		List<ColumnDefinition> newColumnDefinitions = new ArrayList<>();
		for (ColumnDefinition columnDefinition : columnDefinitions) {
			newColumnDefinitions.add(columnDefinition);
			ColumnDefinition newColumnDefinition = new ColumnDefinition();
			newColumnDefinition.setColumnName("`" + TAINT_PREFIX + columnDefinition.getColumnName().replace("\"", "").replace("`", "") + "`");
			String dataType = columnDefinition.getColDataType().getDataType().toUpperCase();
			if (dataType.contains("VARCHAR") || dataType.contains("TEXT") || dataType.contains("NVARCHAR"))
				newColumnDefinition.setColDataType(columnDefinition.getColDataType());
			else {
				ColDataType colDataType = new ColDataType();
				colDataType.setDataType("VARCHAR");
				List<String> argumentsStringList = new ArrayList<>();
				argumentsStringList.add("55");
				colDataType.setArgumentsStringList(argumentsStringList);
				newColumnDefinition.setColDataType(colDataType);
			}
			newColumnDefinitions.add(newColumnDefinition);
		}
		return newColumnDefinitions;
	}

	@Override
	public void visit(CreateView createView) {
		if (createView.getColumnNames() != null)
			createView.setColumnNames(this.taintColumnNames(createView.getColumnNames()));
		if(createView.getSelect() != null) {
			createView.getSelect().getSelectBody().accept(new SelectTainter(this.taints));
		}
	}

	private List<String> taintColumnNames(List<String> columnNames) {
		List<String> newColumnNames = new ArrayList<>();
		for (String columnName : columnNames) {
			newColumnNames.add(columnName);
			newColumnNames.add("`" + TAINT_PREFIX + columnName.replace("\"", "").replace("`", "") + "`");
		}
		return newColumnNames;
	}

	@Override
	public void visit(AlterView alterView) {
		if (alterView.getColumnNames() != null)
			alterView.setColumnNames(this.taintColumnNames(alterView.getColumnNames()));
		if (alterView.getSelectBody() != null)
			alterView.getSelectBody().accept(new SelectTainter(this.taints));
	}

	@Override
	public void visit(Alter alter) {
		for (AlterExpression alterExpression : alter.getAlterExpressions()) {
		    if(alterExpression.getColumnName() != null) {
		    	AlterExpression taintAlterExpression = new AlterExpression();
		    	alter.addAlterExpression(taintAlterExpression);

		    	String taintColumnName =
						"`" + TAINT_PREFIX +
								alterExpression.getColumnName()
								.replace("\"", "")
								.replace("`", "") + "`";
		    	taintAlterExpression.setColumnName(taintColumnName);
		    	taintAlterExpression.setOperation(alterExpression.getOperation());

				List<AlterExpression.ColumnDataType> dataTypes = alterExpression.getColDataTypeList();
				for (AlterExpression.ColumnDataType dataType : dataTypes) {
					if(dataType.getColDataType() != null) {
						String dataTypeString = dataType.getColDataType().getDataType().toUpperCase();
						switch(dataTypeString) {
							case "VARCHAR":
							case "TEXT":
							case "NVARCHAR":
								taintAlterExpression.addColDataType(dataType);
							    break;
							default:
								ColDataType colDataType = new ColDataType();
								colDataType.setDataType("VARCHAR");
								// I ported this code to JsqlParser 2.0 and I wonder what "55" means.
								// If you know more, please add a comment or introduce a constant. - Nico Grashoff
								List<String> argumentsStringList = new ArrayList<>();
								argumentsStringList.add("55");
								colDataType.setArgumentsStringList(argumentsStringList);
							    taintAlterExpression.addColDataType(new AlterExpression.ColumnDataType(taintColumnName, true, colDataType, null));
								break;
						}
					}
				}
			}
		}
	}

	public LinkedHashMap<Integer, Integer> getIndices() {
		if (this.assignmentInfos != null) {
			return this.assignmentInfos.getIndices();
		} else {
			return null;
		}
	}

	private void addTemporaryAssingVariables(List<String> columnNames) {
		if (this.assignmentInfos != null) {
			this.assignmentInfos.setTemporaryAssignVariables(columnNames);
		}
	}

	private void addTemporaryAssignValues(List<AssignmentValue> assignmentValues) {
		if (this.assignmentInfos != null) {
			this.assignmentInfos.setTemporaryAssignValues(assignmentValues);
		}
	}
}
