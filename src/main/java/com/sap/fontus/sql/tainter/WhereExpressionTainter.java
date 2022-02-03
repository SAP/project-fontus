package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;

public class WhereExpressionTainter extends ExpressionVisitorAdapter {

    enum WhereExpressionKind {
        REGULAR,
        QUERY_SUBSELECT_WHERE,
        IN_SUBSELECT_WHERE,
        IN_ASSIGNMENT_SUBSELECT
    }

    private final QueryParameters parameters;
    private WhereExpressionKind kind;
    WhereExpressionTainter(QueryParameters parameters) {
        super();
        this.parameters = parameters;
        this.kind = WhereExpressionKind.REGULAR;
        this.setSelectVisitor(new WhereSubSelectTainter(parameters));
    }

    WhereExpressionTainter(QueryParameters parameters, WhereExpressionKind kind) {
        super();
        this.parameters = parameters;
        this.kind = kind;
        this.setSelectVisitor(new WhereSubSelectTainter(parameters));
    }
    @Override
    public void visit(JdbcParameter jdbcParameter) {
        switch(this.kind) {
            case REGULAR:
                this.parameters.addParameter(ParameterType.WHERE);
                break;
            case IN_SUBSELECT_WHERE:
                this.parameters.addParameter(ParameterType.SUBSELECT_WHERE);
                break;
            case IN_ASSIGNMENT_SUBSELECT:
                this.parameters.addParameter(ParameterType.ASSIGNMENT_SUBSELECT);
                break;
            case QUERY_SUBSELECT_WHERE:
                this.parameters.addParameter(ParameterType.QUERY_SUBSELECT);
                break;
            default:
                throw new IllegalStateException(String.format("Unknown kind: %s", this.kind));
        }
    }
}
