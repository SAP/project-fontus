package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

public class NestedSelectItemTainter extends SelectItemTainter {

    private final List<Expression> plannedExpressions;
    private final List<Table> tables;
    private final List<Expression> where;
    private final List<Join> joins;

    NestedSelectItemTainter(QueryParameters parameters, List<SelectItem> selectItemReference, List<Expression> plannedExpressions, List<Table> tables, List<Expression> where, List<Join> joins) {
        super(parameters, selectItemReference);
        this.plannedExpressions = plannedExpressions;
        this.tables = tables;
        this.where = where;
        this.joins = joins;
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        NestedExpressionTainter tainter = new NestedExpressionTainter(this.parameters, this.expressionReference, this.plannedExpressions, this.tables, this.where, this.joins);
        selectExpressionItem.getExpression().accept(tainter);
        if (!this.expressionReference.isEmpty()) {
            // get new created expression by reference and clear list
            SelectExpressionItem item = new SelectExpressionItem(this.expressionReference.get(0));
            this.expressionReference.clear();
            // copy and add taint prefix for alias
            if (selectExpressionItem.getAlias() != null) {
                //TODO has eventually to be changed
                // TODO(DK): why is that?
                item.setAlias(new Alias(Utils.taintColumnName(selectExpressionItem.getAlias().getName())));
            }
            //'return' selectItem via global list
            this.selectItemReference.add(item);
        }
    }
}
