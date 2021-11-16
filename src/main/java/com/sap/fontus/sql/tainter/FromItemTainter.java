package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.List;

import static com.sap.fontus.Constants.TAINT_PREFIX;

public class FromItemTainter extends FromItemVisitorAdapter {

	private List<Taint> taints;

	FromItemTainter(List<Taint> taints) {
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
	public void visit(ValuesList valuesList) {
		if (valuesList.getColumnNames() != null) {
			List<String> newColumnsList;
			newColumnsList = new ArrayList<>();
			for (String column : valuesList.getColumnNames()) {
				newColumnsList.add(column);
				newColumnsList.add("`" + TAINT_PREFIX + column.replace("\"", "").replace("`", "") + "`");
			}
			valuesList.setColumnNames(newColumnsList);
		}

		if (valuesList.getMultiExpressionList() != null)
			valuesList.getMultiExpressionList().accept(new ItemsListTainter(taints));
	}
}
