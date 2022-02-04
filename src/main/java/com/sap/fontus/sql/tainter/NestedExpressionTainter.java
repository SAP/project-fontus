package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.List;

public class NestedExpressionTainter extends ExpressionTainter {

    List<Expression> plannedExpressions;
    List<Table> tables;
    List<Expression> where;

    public NestedExpressionTainter(QueryParameters parameters, List<Expression> expressionReference, List<Expression> plannedExpressions, List<Table> tables, List<Expression> where) {
        super(parameters, expressionReference);
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
            this.plannedExpressions.add(Utils.getTaintColumn(column));
        } else {
            this.plannedExpressions.add(Utils.getTaintColumn(column.getTable(), column));
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        NestedSelectTainter selectTainter = new NestedSelectTainter(this.parameters, this.plannedExpressions, this.tables, this.where);
        subSelect.getSelectBody().accept(selectTainter);
        if (subSelect.getWithItemsList() != null) {
            for (WithItem withItem : subSelect.getWithItemsList()) {
                NestedSelectTainter innerSelectTainter = new NestedSelectTainter(this.parameters, this.plannedExpressions, this.tables, this.where);
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
