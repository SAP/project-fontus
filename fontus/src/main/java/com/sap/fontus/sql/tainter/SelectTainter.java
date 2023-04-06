package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectTainter extends SelectVisitorAdapter {
	protected final QueryParameters parameters;
	protected final List<Expression> expressionReference;
	protected final List<SelectItem> selectItemReference;

	SelectTainter(QueryParameters parameters) {
		super();
		// List used as Container to return the reference to one newly created
		// Expression by SelectExpressionTainter -> comparable to return object
		this.expressionReference = new ArrayList<>();
		// List used as Container to return the reference to one newly created
		// SelectItem by SelectItemTainter -> comparable to return object
		this.selectItemReference = new ArrayList<>();

		this.parameters = parameters;
	}


	@Override
	public void visit(PlainSelect plainSelect) {
		if (plainSelect.getSelectItems() != null) {
			List<SelectItem> newSelectItems = new ArrayList<>();
			SelectItemTainter selectItemTainter = new SelectItemTainter(this.parameters, this.selectItemReference);
			for (SelectItem selectItem : plainSelect.getSelectItems()) {
				newSelectItems.add(selectItem);

				// Check if nested query exists
				if (selectItem.toString().toLowerCase().startsWith("(select")) {
					// Safe and transform current alias
					Alias alias = ((SelectExpressionItem) selectItem).getAlias();
					Alias newAlias = null;
					if(alias != null) {
						newAlias = new Alias(Utils.taintColumnName(alias.getName()));
					}

					List<Expression> plannedExpressions = new ArrayList<>();
					List<Table> tables = new ArrayList<>();
					List<Expression> where = new ArrayList<>();
					List<Join> joins = new ArrayList<>();
					NestedSelectItemTainter tainter = new NestedSelectItemTainter(this.parameters, this.selectItemReference, plannedExpressions, tables, where, joins);
					selectItem.accept(tainter);


					// Expressions --> columns or values for functions like SUM, AVG, COUNT
					StringBuilder expression = new StringBuilder(10);
					for (Expression e : plannedExpressions) {
						expression.append(e.toString()).append(",");
					}
					if(expression.length() == 0) {
						throw new IllegalStateException("Expression of length 0");
					}
					String expr = expression.substring(0, expression.length()-1);


					// Table of nested query
					String strTable = "";
					String strAlias = "";

					if (tables.size() > 1) {
						System.err.println("Something went wrong, more than one table in nested query!");
					} else {
						Table table = tables.get(0);
						strTable = table.getName();
						Alias a = table.getAlias();
						if(a != null) {
							strAlias = a.getName();
						}

					}

					StringBuilder strJoin = new StringBuilder(0);
					for(Join j : joins) {
						strJoin.append(" ");
						strJoin.append(j);
						strJoin.append(" ");
					}
					String nestedQuery = "";
					String aliasClause = strAlias.isEmpty() ? "" : " as " + strAlias;
					if (where.isEmpty()) {
						nestedQuery = "SELECT " + expr + " FROM " + strTable + aliasClause + strJoin;
					} else {
						nestedQuery = "SELECT " + expr + " FROM " + strTable + aliasClause + strJoin + " WHERE " + where.get(0).toString();
					}
					if(tainter.hasAggregation()) {
						nestedQuery += " LIMIT 1";
					}

					// Yeah let's do SQL injection :D

					try {
						SubSelect sub = new SubSelect();
						sub.setSelectBody(((Select) CCJSqlParserUtil.parse(nestedQuery)).getSelectBody());
						if(alias != null) {
							sub.setAlias(newAlias);
						}
						SelectExpressionItem ie = new SelectExpressionItem();
						ie.setExpression(sub);
						newSelectItems.add(ie);
					} catch (Exception ex) {
						System.err.println(ex);
					}
				} else {
					selectItem.accept(selectItemTainter);
					if (!this.selectItemReference.isEmpty()) {
						// get new created expression by reference in list and clear
						// list
						newSelectItems.add(this.selectItemReference.get(0));
						this.selectItemReference.clear();
					}
				}
			}
			plainSelect.setSelectItems(newSelectItems);
		}
		Expression where = plainSelect.getWhere();
		if(where != null) {
			where.accept(new WhereExpressionTainter(this.parameters));
		}
		GroupByElement groupBy = plainSelect.getGroupBy();
		if(groupBy != null) {
			List<Expression> expressions = groupBy.getGroupByExpressionList().getExpressions();
			List<Expression> taintedExpressions = this.taintGroupBy(expressions);
			groupBy.setGroupByExpressionList(new ExpressionList(taintedExpressions).withUsingBrackets(groupBy.isUsingBrackets()));
		}
		List<Join> joins = plainSelect.getJoins();
		if(joins != null) {
			for (Join join : joins) {
				Collection<Expression> expressions = join.getOnExpressions();
				if(expressions != null) {
					for (Expression expression : expressions) {
						expression.accept(new WhereExpressionTainter(this.parameters));
					}
				}
			}
		}
		FromItem from = plainSelect.getFromItem();
		if(from instanceof SubSelect) {
			SubSelect froms = (SubSelect) from;
			SelectTainter selectTainter = new SelectTainter(this.parameters);
			froms.getSelectBody().accept(selectTainter);
			if(froms != null) {
				System.out.println(froms);
			}

		}
		Limit limit = plainSelect.getLimit();
		if(limit != null) {
			Expression rowCount = limit.getRowCount();
			if(rowCount != null) {
				rowCount.accept(new WhereExpressionTainter(this.parameters));
			}
			Expression offset = limit.getOffset();
			if(offset != null) {
				offset.accept(new WhereExpressionTainter(this.parameters));
			}
		}
	}

	protected List<Expression> taintGroupBy(List<Expression> groupByColumnReferences) {
		if (groupByColumnReferences != null) {
			List<Expression> newColReference;
			newColReference = new ArrayList<>();
			ExpressionTainter set = new ExpressionTainter(this.parameters, this.expressionReference);
			for (Expression expression : groupByColumnReferences) {
				newColReference.add(expression);
				expression.accept(set);
				if (!this.expressionReference.isEmpty()) {
					// get new created expression by reference in list and clear
					// list
					newColReference.add(this.expressionReference.get(0));
					this.expressionReference.clear();
				}
			}
			return newColReference;
		}
		return null;
	}

	@Override
	public void visit(SetOperationList setOperationsList) {
		// offset, fetch, limit, order by not relevant
		if (setOperationsList.getSelects() != null) {
			for (SelectBody selectBody : setOperationsList.getSelects()) {
				selectBody.accept(this);
			}
		}
	}

	@Override
	public void visit(WithItem withItem) {
		SelectTainter selectTainter = new SelectTainter(this.parameters);
		withItem.getSubSelect().getSelectBody().accept(selectTainter);
		if (withItem.getWithItemList() != null) {
			List<SelectItem> newWithItemList = new ArrayList<>();
			SelectItemTainter selectItemTainter = new SelectItemTainter(this.parameters, this.selectItemReference);
			for (SelectItem selectItem : withItem.getWithItemList()) {
				newWithItemList.add(selectItem);
				selectItem.accept(selectItemTainter);
				if(!this.selectItemReference.isEmpty()){
					newWithItemList.add(this.selectItemReference.get(0));
					this.selectItemReference.clear();
				}
			}
			withItem.setWithItemList(newWithItemList);
		}
	}
}
