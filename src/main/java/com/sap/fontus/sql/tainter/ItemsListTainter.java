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

	private List<Taint> taints;

	ItemsListTainter(List<Taint> taints) {
		this.taints = taints;
	}

	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(new SelectTainter(taints));
		if (subSelect.getWithItemsList() != null)
			for (WithItem withItem : subSelect.getWithItemsList()) {
				withItem.accept(new SelectTainter(taints));
			}
	}

	@Override
	public void visit(ExpressionList expressionList) {
		List<Expression> newExpressionList = new ArrayList<>();
		ExpressionTainter insertExpressionTainter = new ExpressionTainter(taints, newExpressionList);
		for (Expression expression : expressionList.getExpressions()) {
			newExpressionList.add(expression);
			expression.accept(insertExpressionTainter);
		}
		expressionList.setExpressions(newExpressionList);
	}

	@Override
	public void visit(MultiExpressionList multiExpressionList) {
		for (ExpressionList expressionList : multiExpressionList.getExprList()) {
			expressionList.accept(this);
		}
	}
}
