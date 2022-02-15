package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

import java.util.Collection;
import java.util.List;

public class WhereSubSelectTainter extends SelectVisitorAdapter {
    private final QueryParameters parameters;

    WhereSubSelectTainter(QueryParameters parameters) {
        super();
        this.parameters = parameters;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        List<Join> joins = plainSelect.getJoins();
        if(joins != null) {
            for(Join join : joins) {
                Collection<Expression> onExpressions = join.getOnExpressions();
                if(onExpressions != null) {
                    for(Expression expression : onExpressions) {
                        expression.accept(new WhereExpressionTainter(this.parameters, WhereExpressionKind.REGULAR));
                    }
                }
            }
        }
        Expression where = plainSelect.getWhere();
        if(where != null) {
            where.accept(new WhereExpressionTainter(this.parameters, WhereExpressionKind.IN_SUBSELECT_WHERE));
        }
    }

}
