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

	private List<Taint> taints;
	private AssignmentInfos assignmentInfos;
	private List<AssignmentValue> columnValues = new ArrayList<>();
	private List<Integer> parameterIndices;

	public StatementTainter(List<Taint> taints) {
		this.taints = taints;
		this.assignmentInfos = new AssignmentInfos();
		this.parameterIndices = new ArrayList<>();
	}

	public Map<String, String> getAssignmentInfos() {
		return assignmentInfos.getAssignmentInfosAsString();
	}

	@Override
	public void visit(Statements statements) {
		for (Statement statement : statements.getStatements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(Select select) {
		SelectTainter selectTainter = new SelectTainter(taints);
		selectTainter.setAssignmentValues(columnValues);
		select.getSelectBody().accept(selectTainter);
		if (select.getWithItemsList() != null)
			for (WithItem withItem : select.getWithItemsList()) {
				SelectTainter innerSelectTainter = new SelectTainter(taints);
				innerSelectTainter.setAssignmentValues(this.columnValues);
				withItem.accept(innerSelectTainter);
			}
	}

	@Override
	public void visit(Insert insert) {
		columnValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();
		if (insert.getColumns() != null) {
			insert.setColumns(taintColumns(insert.getColumns()));
			for (Column c : insert.getColumns()) {
				columnNames.add(c.getColumnName());
			}
		}
		addTemporaryAssingVariables(columnNames);
		if (insert.getItemsList() != null) {
			ItemsListTainter itemListTainter = new ItemsListTainter(taints);
			itemListTainter.setAssignmentValues(columnValues);
			// Loop after setColumns will also include new taint columns if needed
			insert.getItemsList().accept(itemListTainter);
		}
		if (insert.getSelect() != null)
			insert.getSelect().accept(this);
		if (insert.getReturningExpressionList() != null)
			insert.setReturningExpressionList(taintReturningExpression(insert.getReturningExpressionList()));
		addTemporaryAssignValues(columnValues);
		//System.out.println("Insert");
		//assignmentInfos.getAssignmentInfosAsString().forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
	}

	@Override
	public void visit(Update update) {
		columnValues = new ArrayList<>();
		List<String> columnNames = new ArrayList<>();
		// Parser breaks if !update.isUseColumnBrackets() and unrecognized
		// update.isUseSelect()
		// for example: update a set id = (select id from b);

		List<UpdateSet> updateSets = update.getUpdateSets();
		List<Column> taintedCols = new ArrayList<>();
		List<Expression> taintedExprs = new ArrayList<>();
		for(UpdateSet updateSet : updateSets) {
			List<Column> columns = updateSet.getColumns();
			ArrayList<Column> tcols = (ArrayList<Column>) taintColumns(columns);

			for (Column c : tcols) {
				columnNames.add(c.getColumnName());
			}

			List<Expression> expressions = updateSet.getExpressions();
			Expression expr = expressions.get(0);
			ArrayList<Expression> texprs = (ArrayList<Expression>) taintExpressions(updateSet.getExpressions());

			if(expr instanceof SubSelect) {
				updateSet.setColumns(tcols);
				updateSet.setExpressions(texprs);
			} else {
				if(texprs.size() <= 1) {
					for(Column c : tcols) {
						System.out.println(c.toString());
					}
					for(Expression e : texprs) {
						System.out.println(e.toString());
					}
				}
				taintedCols.add(tcols.get(1));
				taintedExprs.add(texprs.get(1));

			}
			//update.addUpdateSet(cols.get(1), exprs.get(1));
		}
		for (int i = 0; i < taintedCols.size(); i++) {
			update.addUpdateSet(taintedCols.get(i), taintedExprs.get(i));
		}

		addTemporaryAssingVariables(columnNames);
		addTemporaryAssignValues(columnValues);
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
			update.getFromItem().accept(new FromItemTainter(taints));

	}

	@Override
	public void visit(Replace replace) {
		if (replace.getColumns() != null) {
			replace.setColumns(taintColumns(replace.getColumns()));
		}
		if (replace.getExpressions() != null) {
			replace.setExpressions(taintExpressions(replace.getExpressions()));
		}
		if (replace.getItemsList() != null) {
			ItemsListTainter itemsListTainter = new ItemsListTainter(taints);
			replace.getItemsList().accept(itemsListTainter);
		}
	}

	@Override
	public void visit(Merge merge) {
		if (merge.getMergeInsert() != null) {
			merge.getMergeInsert().setColumns(taintColumns(merge.getMergeInsert().getColumns()));
			merge.getMergeInsert().setValues(taintExpressions(merge.getMergeInsert().getValues()));
		}
		if (merge.getMergeUpdate() != null) {
			merge.getMergeUpdate().setColumns(taintColumns(merge.getMergeUpdate().getColumns()));
			merge.getMergeUpdate().setValues(taintExpressions(merge.getMergeUpdate().getValues()));
		}
		if (merge.getUsingSelect() != null) {
			merge.getUsingSelect().accept(new ItemsListTainter(taints));
		}
	}

	@Override
	public void visit(Execute execute) {
		if (execute.getExprList() != null)
			execute.getExprList().accept(new ItemsListTainter(taints));
	}

	private List<Expression> taintExpressions(List<Expression> expressions) {
		List<Expression> newExpressions = new ArrayList<>();
		List<Expression> expressionReference = new ArrayList<>(1);
		ExpressionTainter expressionTainter = new ExpressionTainter(taints, expressionReference);
		expressionTainter.setAssignmentValues(columnValues);
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
		SelectItemTainter selectItemTainter = new SelectItemTainter(taints, selectItemReference);
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
		ExpressionTainter expressionTainter = new ExpressionTainter(taints, expressionReference);
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
			createTable.setColumnDefinitions(taintColumnDefinitions(createTable.getColumnDefinitions()));
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
			createView.setColumnNames(taintColumnNames(createView.getColumnNames()));
		if(createView.getSelect() != null) {
			createView.getSelect().getSelectBody().accept(new SelectTainter(taints));
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
			alterView.setColumnNames(taintColumnNames(alterView.getColumnNames()));
		if (alterView.getSelectBody() != null)
			alterView.getSelectBody().accept(new SelectTainter(taints));
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
		if (assignmentInfos != null) {
			return assignmentInfos.getIndices();
		} else {
			return null;
		}
	}

	private void addTemporaryAssingVariables(List<String> columnNames) {
		if (assignmentInfos != null) {
			assignmentInfos.setTemporaryAssignVariables(columnNames);
		}
	}

	private void addTemporaryAssignValues(List<AssignmentValue> assignmentValues) {
		if (assignmentInfos != null) {
			assignmentInfos.setTemporaryAssignValues(assignmentValues);
		}
	}
}
