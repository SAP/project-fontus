package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

public class NestedSelectTainter extends SelectTainter {

    private final List<Expression> plannedExpressions;
    private final List<Table> tables;
    private final List<Expression> where;
    private final List<Join> joins;
    private boolean hasAggregation = false;

    public NestedSelectTainter(QueryParameters parameters, List<Expression> plannedExpressions, List<Table> tables, List<Expression> where, List<Join> joins) {
        super(parameters);
        this.tables = tables;
        this.plannedExpressions = plannedExpressions;
        this.where = where;
        this.joins = joins;
    }
    public boolean hasAggregation() {
        return this.hasAggregation;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null) {

            if (plainSelect.getWhere() != null) {
                this.where.add(plainSelect.getWhere());
            }

            if (plainSelect.getFromItem() instanceof Table) {
                this.tables.add((Table) plainSelect.getFromItem());
            }

            List<Join> js = plainSelect.getJoins();
            if(js != null) {
                this.joins.addAll(js);
            }

            List<SelectItem> newSelectItems = new ArrayList<>();
            NestedSelectItemTainter selectItemTainter = new NestedSelectItemTainter(this.parameters, this.selectItemReference, this.plannedExpressions, this.tables, this.where, this.joins);
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                newSelectItems.add(selectItem);
                if (selectItem.toString().toLowerCase().contains("(select")) {
                    NestedSelectItemTainter nsit = new NestedSelectItemTainter(this.parameters, this.selectItemReference, this.plannedExpressions, this.tables, this.where, this.joins);
                    selectItem.accept(nsit);
                    this.hasAggregation |= nsit.hasAggregation();
                    if (selectItem instanceof SelectExpressionItem) {
                        SelectExpressionItem sei = new SelectExpressionItem(((SelectExpressionItem) selectItem).getExpression());
                        Alias al = sei.getAlias();
                        if (sei.getExpression() instanceof SubSelect) {
                            SelectBody selBody = ((SubSelect) sei.getExpression()).getSelectBody();

                        }
                    }
                } else {
                    selectItem.accept(selectItemTainter);
                    this.hasAggregation |= selectItemTainter.hasAggregation();
                    if (!this.selectItemReference.isEmpty()) {
                        // get new created expression by reference in list and clear
                        // list
                        newSelectItems.add(this.selectItemReference.get(0));
                        this.selectItemReference.clear();
                    }
                }
            }
            plainSelect.setSelectItems(newSelectItems);
        }
        Expression w = plainSelect.getWhere();
        if(w != null) {
            w.accept(new WhereExpressionTainter(this.parameters, WhereExpressionKind.QUERY_SUBSELECT_WHERE));
        }

        GroupByElement groupBy = plainSelect.getGroupBy();
        if(groupBy != null) {
            List<Expression> groupByExpressions = groupBy.getGroupByExpressionList().getExpressions();
            List<Expression> taintedGroupByExprs = super.taintGroupBy(groupByExpressions);
            groupBy.setGroupByExpressionList(new ExpressionList(taintedGroupByExprs).withUsingBrackets(groupBy.isUsingBrackets()));
        }
    }
}
