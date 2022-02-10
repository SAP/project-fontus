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

	SelectItemTainter(QueryParameters parameters, List<SelectItem> selectItemReference) {
		super();
		// List used as Container to return the reference to one newly created
		// Expression by SelectExpressionTainter -> comparable to return object
		this.expressionReference = new ArrayList<>();
		this.selectItemReference = selectItemReference;
		this.parameters = parameters;
	}

	@Override
	public void visit(SelectExpressionItem selectExpressionItem) {
		ExpressionTainter tainter = new ExpressionTainter(this.parameters, this.expressionReference);
		selectExpressionItem.getExpression().accept(tainter);
		if (!this.expressionReference.isEmpty()) {
			// get new created expression by reference and clear list
			SelectExpressionItem item = new SelectExpressionItem(this.expressionReference.get(0));
			this.expressionReference.clear();
			// copy and add taint prefix for alias
			if (selectExpressionItem.getAlias() != null) {
				item.setAlias(new Alias(Utils.taintColumnName(selectExpressionItem.getAlias().getName())));
			}
			//'return' selectItem via global list
			this.selectItemReference.add(item);
		}
	}
}
