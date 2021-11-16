package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

import static com.sap.fontus.Constants.TAINT_PREFIX;

public class SelectItemTainter extends SelectItemVisitorAdapter {

	private List<Taint> taints;
	private List<SelectItem> selectItemReference;
	private List<Expression> expressionReference;

	SelectItemTainter(List<Taint> taints, List<SelectItem> selectItemReference) {
		this.taints = taints;
		// List used as Container to return the reference to one newly created
		// Expression by SelectExpressionTainter -> comparable to return object
		this.expressionReference = new ArrayList<>();
		this.selectItemReference = selectItemReference;
	}

	@Override
	public void visit(SelectExpressionItem selectExpressionItem) {
		ExpressionTainter selectExpressionTainter = new ExpressionTainter(taints, expressionReference);
		selectExpressionItem.getExpression().accept(selectExpressionTainter);
		if (!expressionReference.isEmpty()) {
			// get new created expression by reference and clear list
			SelectExpressionItem item = new SelectExpressionItem(expressionReference.get(0));
			expressionReference.clear();
			// copy and add taint prefix for alias
			if (selectExpressionItem.getAlias() != null)
				item.setAlias(new Alias("`" + TAINT_PREFIX + selectExpressionItem.getAlias().getName().replace("\"", "").replace("`", "") + "`"));
			//'return' selectItem via global list
			selectItemReference.add(item);
		}
	}
}
