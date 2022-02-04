package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectItemTainter extends SelectItemVisitorAdapter {
	protected final QueryParameters parameters;
	protected final List<SelectItem> selectItemReference;
	protected final List<Expression> expressionReference;
	protected List<AssignmentValue> assignmentValues;

	SelectItemTainter(QueryParameters parameters, List<SelectItem> selectItemReference) {
		// List used as Container to return the reference to one newly created
		// Expression by SelectExpressionTainter -> comparable to return object
		this.expressionReference = new ArrayList<>();
		this.selectItemReference = selectItemReference;
		this.parameters = parameters;
	}

	public List<AssignmentValue> getAssignmentValues() {
		return this.assignmentValues;
	}

	public void setAssignmentValues(List<AssignmentValue> assignmentValues) {
		this.assignmentValues = assignmentValues;
	}

	@Override
	public void visit(SelectExpressionItem selectExpressionItem) {
		ExpressionTainter selectExpressionTainter = new ExpressionTainter(this.parameters, this.expressionReference);
		selectExpressionTainter.setAssignmentValues(this.assignmentValues);
		selectExpressionItem.getExpression().accept(selectExpressionTainter);
		if (!this.expressionReference.isEmpty()) {
			// get new created expression by reference and clear list
			SelectExpressionItem item = new SelectExpressionItem(this.expressionReference.get(0));
			this.expressionReference.clear();
			// copy and add taint prefix for alias
			if (selectExpressionItem.getAlias() != null) {
				//assignmentValues.add(new AssignmentValue(selectExpressionItem.getAlias().getName()));
				item.setAlias(new Alias(Utils.taintColumnName(selectExpressionItem.getAlias().getName())));
			}
			//'return' selectItem via global list
			this.selectItemReference.add(item);
		}
	}
}
