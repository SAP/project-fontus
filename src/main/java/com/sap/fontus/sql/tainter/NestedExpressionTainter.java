package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.List;

public class NestedExpressionTainter extends ExpressionTainter {

    private final List<Expression> plannedExpressions;
    private final List<Table> tables;
    private final List<Expression> where;
    private final List<Join> joins;
    private boolean hasAggregation = false;

    public NestedExpressionTainter(QueryParameters parameters, List<Expression> expressionReference, List<Expression> plannedExpressions, List<Table> tables, List<Expression> where, List<Join> joins) {
        super(parameters, expressionReference);
        this.plannedExpressions = plannedExpressions;
        this.tables = tables;
        this.where = where;
        this.joins = joins;
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
        this.parameters.begin(StatementType.SUB_SELECT);
        NestedSelectTainter selectTainter = new NestedSelectTainter(this.parameters, this.plannedExpressions, this.tables, this.where, this.joins);
        subSelect.getSelectBody().accept(selectTainter);
        this.hasAggregation |= selectTainter.hasAggregation();
        if (subSelect.getWithItemsList() != null) {
            for (WithItem withItem : subSelect.getWithItemsList()) {
                NestedSelectTainter innerSelectTainter = new NestedSelectTainter(this.parameters, this.plannedExpressions, this.tables, this.where, this.joins);
                withItem.accept(innerSelectTainter);
                this.hasAggregation |= innerSelectTainter.hasAggregation();
            }
        }
        this.parameters.end(StatementType.SUB_SELECT);
    }

    @Override
    public void visit(Function arg0) {
        this.hasAggregation = true;
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(SignedExpression arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(Addition arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(Division arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(Multiplication arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(Subtraction arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(AndExpression arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(OrExpression arg0) {
        // add '0' for correct column count
        this.expressionReference.add(new StringValue("'0'"));
    }

    @Override
    public void visit(Between arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(EqualsTo arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(GreaterThan arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(GreaterThanEquals arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(LikeExpression arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(MinorThan arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(MinorThanEquals arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }

    @Override
    public void visit(NotEqualsTo arg0) {
        // add '0' for correct column count
        this.plannedExpressions.add(new StringValue("'0'"));
    }
    public boolean hasAggregation() {
        return this.hasAggregation;
    }
}
