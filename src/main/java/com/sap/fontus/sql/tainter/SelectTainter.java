package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

public class SelectTainter extends SelectVisitorAdapter {

	private List<Taint> taints;
	private List<Expression> expressionReference;
	private List<SelectItem> selectItemReference;

	SelectTainter(List<Taint> taints) {
		this.taints = taints;
		// List used as Container to return the reference to one newly created
		// Expression by SelectExpressionTainter -> comparable to return object
		expressionReference = new ArrayList<>();
		// List used as Container to return the reference to one newly created
		// SelectItem by SelectItemTainter -> comparable to return object
		selectItemReference = new ArrayList<>();
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		if (plainSelect.getSelectItems() != null) {
			List<SelectItem> newSelectItems = new ArrayList<>();
			SelectItemTainter selectItemTainter = new SelectItemTainter(taints, selectItemReference);
			for (SelectItem selectItem : plainSelect.getSelectItems()) {
				newSelectItems.add(selectItem);
				selectItem.accept(selectItemTainter);
				if (!selectItemReference.isEmpty()) {
					// get new created expression by reference in list and clear
					// list
					newSelectItems.add(selectItemReference.get(0));
					selectItemReference.clear();
				}
			}
			plainSelect.setSelectItems(newSelectItems);
		}

		GroupByElement groupBy = plainSelect.getGroupBy();
		if(groupBy != null) {
			List<Expression> groupByExpressions = groupBy.getGroupByExpressions();
			List<Expression> taintedGroupByExpressions = taintGroupBy(groupByExpressions);
			groupBy.setGroupByExpressions(taintedGroupByExpressions);
		}
	}

	private List<Expression> taintGroupBy(List<Expression> groupByColumnReferences) {
		if (groupByColumnReferences != null) {
			List<Expression> newGroupByColumnReferences;
			newGroupByColumnReferences = new ArrayList<>();
			ExpressionTainter selectExpressionTainter = new ExpressionTainter(taints, expressionReference);
			for (Expression expression : groupByColumnReferences) {
				newGroupByColumnReferences.add(expression);
				expression.accept(selectExpressionTainter);
				if (!expressionReference.isEmpty()) {
					// get new created expression by reference in list and clear
					// list
					newGroupByColumnReferences.add(expressionReference.get(0));
					expressionReference.clear();
				}
			}
			return newGroupByColumnReferences;
		}
		return null;
	}

	@Override
	public void visit(SetOperationList setOperationsList) {
		// offset, fetch, limit, order by not relevant
		if (setOperationsList.getSelects() != null)
			for (SelectBody selectBody : setOperationsList.getSelects()) {
				selectBody.accept(this);
			}
	}

	@Override
	public void visit(WithItem withItem) {
		SelectTainter selectTainter = new SelectTainter(taints);
		withItem.getSubSelect().getSelectBody().accept(selectTainter);
		if (withItem.getWithItemList() != null) {
			List<SelectItem> newWithItemList = new ArrayList<>();
			SelectItemTainter selectItemTainter = new SelectItemTainter(taints, selectItemReference);
			for (SelectItem selectItem : withItem.getWithItemList()) {
				newWithItemList.add(selectItem);
				selectItem.accept(selectItemTainter);
				if(!selectItemReference.isEmpty()){
					newWithItemList.add(selectItemReference.get(0));
					selectItemReference.clear();
				}
			}
			withItem.setWithItemList(newWithItemList);
		}
	}
}
