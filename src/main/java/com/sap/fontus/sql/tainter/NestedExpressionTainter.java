package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.List;

import static com.sap.fontus.Constants.TAINT_PREFIX;

public class NestedExpressionTainter extends ExpressionTainter {

    List<Expression> plannedExpressions;
    List<Table> tables;
    List<Expression> where;

    public NestedExpressionTainter(List<Taint> taints, List<Expression> expressionReference, List<Expression> plannedExpressions, List<Table> tables, List<Expression> where) {
        super(taints, expressionReference);
        this.plannedExpressions = plannedExpressions;
        this.tables = tables;
        this.where = where;
    }

    @Override
    public void visit(Column column) {
        if (column.getColumnName().compareToIgnoreCase("DEFAULT") == 0
                || column.getColumnName().compareToIgnoreCase("FALSE") == 0
                || column.getColumnName().compareToIgnoreCase("TRUE") == 0) {
            //Missmatched keywords, that are seen as columns, but are values
            this.plannedExpressions.add(new StringValue("'0'"));
        } else if (column.getTable() == null || column.getTable().getName() == null) {
            // 'return' expression via global list
            Column newColumn = new Column("`" + TAINT_PREFIX + column.getColumnName().replace("\"", "").replace("`", "") + "`");
            this.plannedExpressions.add(newColumn);
        } else {
            this.plannedExpressions.add(new Column(column.getTable() + "." + "`" + TAINT_PREFIX
                    + column.getColumnName().replace("\"", "").replace("`", "") + "`"));
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        NestedSelectTainter selectTainter = new NestedSelectTainter(super.taints, this.plannedExpressions, this.tables, this.where);
        selectTainter.setAssignmentValues(super.assignmentValues);
        subSelect.getSelectBody().accept(selectTainter);
        if (subSelect.getWithItemsList() != null) {
            for (WithItem withItem : subSelect.getWithItemsList()) {
                NestedSelectTainter innerSelectTainter = new NestedSelectTainter(super.taints, this.plannedExpressions, this.tables, this.where);
                innerSelectTainter.setAssignmentValues(super.assignmentValues);
                withItem.accept(innerSelectTainter);
            }
        }
    }

    @Override
    public void visit(Function arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }
}
