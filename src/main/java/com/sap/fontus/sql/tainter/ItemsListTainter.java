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
	private List<AssignmentValue> assignmentValues;

	ItemsListTainter(List<Taint> taints) {
		this.taints = taints;
		//this.assignmentValues = new ArrayList<>();
	}

	public List<AssignmentValue> getAssignmentValues() {
		return assignmentValues;
	}

	public void setAssignmentValues(List<AssignmentValue> assignmentValues) {
		this.assignmentValues = assignmentValues;
	}

	@Override
	public void visit(SubSelect subSelect) {
		SelectTainter selectTainter =  new SelectTainter(taints);
		selectTainter.setAssignmentValues(assignmentValues);
		subSelect.getSelectBody().accept(selectTainter);
		if (subSelect.getWithItemsList() != null)
			for (WithItem withItem : subSelect.getWithItemsList()) {
				SelectTainter innerSelectTainter = new SelectTainter(taints);
				innerSelectTainter.setAssignmentValues(assignmentValues);
				withItem.accept(innerSelectTainter);
			}
	}

	@Override
	public void visit(ExpressionList expressionList) {
		List<Expression> newExpressionList = new ArrayList<>();
		ExpressionTainter insertExpressionTainter = new ExpressionTainter(taints, newExpressionList);
		insertExpressionTainter.setAssignmentValues(assignmentValues);
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
