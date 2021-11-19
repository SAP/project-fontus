package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

public class SelectTainter extends SelectVisitorAdapter {

	private final List<Taint> taints;
	private final List<Expression> expressionReference;
	private final List<SelectItem> selectItemReference;
	private List<AssignmentValue> assignmentValues;

	SelectTainter(List<Taint> taints) {
		this.taints = taints;
		// List used as Container to return the reference to one newly created
		// Expression by SelectExpressionTainter -> comparable to return object
		this.expressionReference = new ArrayList<>();
		// List used as Container to return the reference to one newly created
		// SelectItem by SelectItemTainter -> comparable to return object
		this.selectItemReference = new ArrayList<>();
	}

	public List<AssignmentValue> getAssignmentValues() {
		return this.assignmentValues;
	}

	public void setAssignmentValues(List<AssignmentValue> assignmentValues) {
		this.assignmentValues = assignmentValues;
	}

	@Override
	public void visit(PlainSelect plainSelect) {
		if (plainSelect.getSelectItems() != null) {
			List<SelectItem> newSelectItems = new ArrayList<>();
			SelectItemTainter selectItemTainter = new SelectItemTainter(this.taints, this.selectItemReference);
			selectItemTainter.setAssignmentValues(this.assignmentValues);
			for (SelectItem selectItem : plainSelect.getSelectItems()) {
				newSelectItems.add(selectItem);
				selectItem.accept(selectItemTainter);
				if (!this.selectItemReference.isEmpty()) {
					// get new created expression by reference in list and clear
					// list
					newSelectItems.add(this.selectItemReference.get(0));
					this.selectItemReference.clear();
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
			ExpressionTainter selectExpressionTainter = new ExpressionTainter(this.taints, this.expressionReference);
			selectExpressionTainter.setAssignmentValues(this.assignmentValues);
			for (Expression expression : groupByColumnReferences) {
				newGroupByColumnReferences.add(expression);
				expression.accept(selectExpressionTainter);
				if (!this.expressionReference.isEmpty()) {
					// get new created expression by reference in list and clear
					// list
					newGroupByColumnReferences.add(this.expressionReference.get(0));
					this.expressionReference.clear();
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
		SelectTainter selectTainter = new SelectTainter(this.taints);
		selectTainter.setAssignmentValues(this.assignmentValues);
		withItem.getSubSelect().getSelectBody().accept(selectTainter);
		if (withItem.getWithItemList() != null) {
			List<SelectItem> newWithItemList = new ArrayList<>();
			SelectItemTainter selectItemTainter = new SelectItemTainter(this.taints, this.selectItemReference);
			selectItemTainter.setAssignmentValues(this.assignmentValues);
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
