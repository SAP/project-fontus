package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

public class WhereSubSelectTainter extends SelectVisitorAdapter {
    private QueryParameters parameters;

    WhereSubSelectTainter(QueryParameters parameters) {
        super();
        this.parameters = parameters;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        Expression where = plainSelect.getWhere();
        if(where != null) {
            where.accept(new WhereExpressionTainter(this.parameters, WhereExpressionTainter.WhereExpressionKind.IN_SUBSELECT_WHERE));
        }
    }

}
