package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.List;

public class ItemsListTainter extends ItemsListVisitorAdapter {
	private final QueryParameters parameters;

	ItemsListTainter(QueryParameters parameters) {
		super();
		this.parameters = parameters;
	}


	@Override
	public void visit(SubSelect subSelect) {
		SelectTainter selectTainter =  new SelectTainter(this.parameters);
		subSelect.getSelectBody().accept(selectTainter);
		if (subSelect.getWithItemsList() != null) {
			for (WithItem withItem : subSelect.getWithItemsList()) {
				SelectTainter innerSelectTainter = new SelectTainter(this.parameters);
				withItem.accept(innerSelectTainter);
			}
		}
	}

	@Override
	public void visit(ExpressionList expressionList) {
		List<Expression> newExpressionList = new ArrayList<>();
		ExpressionTainter tainter = new ExpressionTainter(this.parameters, newExpressionList);
		for (Expression expression : expressionList.getExpressions()) {
			newExpressionList.add(expression);
			expression.accept(tainter);
		}
		expressionList.setExpressions(newExpressionList);
	}

	@Override
	public void visit(MultiExpressionList multiExpressionList) {
		for (ExpressionList expressionList : multiExpressionList.getExpressionLists()) {
			expressionList.accept(this);
		}
	}
}
