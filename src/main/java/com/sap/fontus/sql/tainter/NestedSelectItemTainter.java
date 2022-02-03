package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

import static com.sap.fontus.Constants.TAINT_PREFIX;

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
        selectNestedExpressionTainter.setAssignmentValues(this.assignmentValues);
        selectExpressionItem.getExpression().accept(selectNestedExpressionTainter);
        if (!this.expressionReference.isEmpty()) {
            // get new created expression by reference and clear list
            SelectExpressionItem item = new SelectExpressionItem(this.expressionReference.get(0));
            this.expressionReference.clear();
            // copy and add taint prefix for alias
            if (selectExpressionItem.getAlias() != null) {
                //assignmentValues.add(new AssignmentValue(selectExpressionItem.getAlias().getName()));
                //TODO has eventually to be changed
                item.setAlias(new Alias("`" + TAINT_PREFIX + selectExpressionItem.getAlias().getName().replace("\"", "").replace("`", "") + "`"));
            }
            //'return' selectItem via global list
            this.selectItemReference.add(item);
        }
    }
}
