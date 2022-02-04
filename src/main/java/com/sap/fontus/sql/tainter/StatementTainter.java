package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.create.table.ColDataType;
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
import java.util.List;

public class StatementTainter extends StatementVisitorAdapter {

	private final QueryParameters parameters;

	public StatementTainter() {
		super();
		this.parameters = new QueryParameters();
	}

	public QueryParameters getParameters() {
		return this.parameters;
	}


	@Override
	public void visit(Statements stmts) {
		for (Statement statement : stmts.getStatements()) {
			statement.accept(this);
		}
	}

	@Override
	public void visit(Select select) {
		SelectTainter selectTainter = new SelectTainter(this.parameters);
		select.getSelectBody().accept(selectTainter);
		if (select.getWithItemsList() != null) {
			for (WithItem withItem : select.getWithItemsList()) {
				SelectTainter innerSelectTainter = new SelectTainter(this.parameters);
				withItem.accept(innerSelectTainter);
			}
		}
	}

	@Override
	public void visit(Insert insert) {
		if (insert.getColumns() != null) {
			insert.setColumns(this.taintColumns(insert.getColumns()));
		}
		if (insert.getItemsList() != null) {
			ItemsListTainter itemListTainter = new ItemsListTainter(this.parameters);
			// Loop after setColumns will also include new taint columns if needed
			insert.getItemsList().accept(itemListTainter);
		}
		if (insert.getSelect() != null) {
			insert.getSelect().accept(this);
		}
		if (insert.getReturningExpressionList() != null) {
			insert.setReturningExpressionList(this.taintReturningExpression(insert.getReturningExpressionList()));
		}
		//System.out.println("Insert");
		//assignmentInfos.getAssignmentInfosAsString().forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
	}

	@Override
	public void visit(Update update) {
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
				for(int i = 0; i < columns.size(); i++) {
					UpdateSet us = new UpdateSet();
					ArrayList<Column> cols = new ArrayList<>();
					cols.add(tcols.get(i));
					us.setColumns(cols);
					ArrayList<Expression> exprs = new ArrayList<>();
					exprs.add(texprs.get(i));
					us.setExpressions(exprs);
					us.setUsingBracketsForColumns(true);
					fixedSets.add(us);
				}
			} else {
				for (int i = 0; i < tcols.size(); i++) {
					Column c = tcols.get(i);
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

		if (update.getFromItem() != null) {
			update.getFromItem().accept(new FromItemTainter(this.parameters));
		}

		if(update.getWhere() != null) {
			update.getWhere().accept(new WhereExpressionTainter(this.parameters));
		}

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
			ItemsListTainter itemsListTainter = new ItemsListTainter(this.parameters);
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
			merge.getUsingSelect().accept(new ItemsListTainter(this.parameters));
		}
	}

	@Override
	public void visit(Execute execute) {
		if (execute.getExprList() != null) {
			execute.getExprList().accept(new ItemsListTainter(this.parameters));
		}
	}

	private List<Expression> taintExpressions(List<Expression> expressions) {
		List<Expression> newExpressions = new ArrayList<>();
		List<Expression> expressionReference = new ArrayList<>(1);
		ExpressionTainter expressionTainter = new ExpressionTainter(this.parameters, expressionReference);
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
		SelectItemTainter selectItemTainter = new SelectItemTainter(this.parameters, selectItemReference);
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
		ExpressionTainter expressionTainter = new ExpressionTainter(this.parameters, expressionReference);
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
		if (createTable.getColumnDefinitions() != null) {
			createTable.setColumnDefinitions(Utils.taintColumnDefinitions(createTable.getColumnDefinitions()));
		}
		if (createTable.getSelect() != null) {
			createTable.getSelect().accept(this);
		}
	}

	@Override
	public void visit(CreateView createView) {
		if (createView.getColumnNames() != null) {
			createView.setColumnNames(Utils.taintColumnNames(createView.getColumnNames()));
		}
		if(createView.getSelect() != null) {
			createView.getSelect().getSelectBody().accept(new SelectTainter(this.parameters));
		}
	}

	@Override
	public void visit(AlterView alterView) {
		if (alterView.getColumnNames() != null) {
			alterView.setColumnNames(Utils.taintColumnNames(alterView.getColumnNames()));
		}
		if (alterView.getSelectBody() != null) {
			alterView.getSelectBody().accept(new SelectTainter(this.parameters));
		}
	}

	@Override
	public void visit(Alter alter) {
		for (AlterExpression alterExpression : alter.getAlterExpressions()) {
		    if(alterExpression.getColumnName() != null) {
		    	AlterExpression taintAlterExpression = new AlterExpression();
		    	alter.addAlterExpression(taintAlterExpression);

		    	String taintColumnName = Utils.taintColumnName(alterExpression.getColumnName());
		    	taintAlterExpression.setColumnName(taintColumnName);
		    	taintAlterExpression.setOperation(alterExpression.getOperation());

				List<AlterExpression.ColumnDataType> dataTypes = alterExpression.getColDataTypeList();
				for (AlterExpression.ColumnDataType dataType : dataTypes) {
					if(dataType.getColDataType() != null) {
						String dataTypeString = dataType.getColDataType().getDataType().toUpperCase();
						switch(dataTypeString) {
							//case "VARCHAR":
							case "TEXT":
							//case "NVARCHAR":
								taintAlterExpression.addColDataType(dataType);
							    break;
							default:
								ColDataType colDataType = new ColDataType();
								colDataType.setDataType("TEXT");
							    taintAlterExpression.addColDataType(new AlterExpression.ColumnDataType(taintColumnName, true, colDataType, null));
								break;
						}
					}
				}
			}
		}
	}

}
