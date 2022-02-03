package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.ArrayList;
import java.util.List;

import static com.sap.fontus.Constants.TAINT_PREFIX;

public class FromItemTainter extends FromItemVisitorAdapter {
	private final QueryParameters parameters;
	private List<AssignmentValue> assignmentValues;

	FromItemTainter(QueryParameters parameters) {
		super();
		this.parameters = parameters;
	}

	public List<AssignmentValue> getAssignmentValues() {
		return this.assignmentValues;
	}

	public void setAssignmentValues(List<AssignmentValue> assignmentValues) {
		this.assignmentValues = assignmentValues;
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
				newColumnsList.add("`" + TAINT_PREFIX + column.replace("\"", "").replace("`", "") + "`");
			}
			valuesList.setColumnNames(newColumnsList);
		}

		if (valuesList.getMultiExpressionList() != null)
			valuesList.getMultiExpressionList().accept(new ItemsListTainter(this.parameters));
	}
}
