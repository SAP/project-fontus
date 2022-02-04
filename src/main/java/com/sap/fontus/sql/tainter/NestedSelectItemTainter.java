package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

public class NestedSelectItemTainter extends SelectItemTainter {

    List<Expression> plannedExpressions;
    List<Table> tables;
    List<Expression> where;

    NestedSelectItemTainter(QueryParameters parameters, List<SelectItem> selectItemReference, List<Expression> plannedExpressions, List<Table> tables, List<Expression> where) {
        super(parameters, selectItemReference);
        this.plannedExpressions = plannedExpressions;
        this.tables = tables;
        this.where = where;
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        NestedExpressionTainter selectNestedExpressionTainter = new NestedExpressionTainter(this.parameters, this.expressionReference, this.plannedExpressions, this.tables, this.where);
        selectExpressionItem.getExpression().accept(selectNestedExpressionTainter);
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
