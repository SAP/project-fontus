package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.List;

public class FromItemTainter extends FromItemVisitorAdapter {
	private final QueryParameters parameters;

	FromItemTainter(QueryParameters parameters) {
		super();
		this.parameters = parameters;
	}

	@Override
	public void visit(SubSelect subSelect) {
		subSelect.getSelectBody().accept(new SelectTainter(this.parameters));
		if (subSelect.getWithItemsList() != null)
			for (WithItem withItem : subSelect.getWithItemsList()) {
				withItem.accept(new SelectTainter(this.parameters));
			}
	}

	@Override
	public void visit(ValuesList valuesList) {
		if (valuesList.getColumnNames() != null) {
			List<String> newColumnsList;
			newColumnsList = new ArrayList<>();
			for (String column : valuesList.getColumnNames()) {
				newColumnsList.add(column);
				newColumnsList.add(Utils.taintColumnName(column));
			}
			valuesList.setColumnNames(newColumnsList);
		}

		if (valuesList.getMultiExpressionList() != null)
			valuesList.getMultiExpressionList().accept(new ItemsListTainter(this.parameters));
	}
}
