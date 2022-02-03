package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

public class NestedSelectTainter extends SelectTainter {

    List<Expression> plannedExpressions;
    List<Table> tables;
    List<Expression> where;

    public NestedSelectTainter(QueryParameters parameters, List<Expression> plannedExpressions, List<Table> tables, List<Expression> where) {
        super(parameters);
        this.tables = tables;
        this.plannedExpressions = plannedExpressions;
        this.where = where;
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.getSelectItems() != null) {

            if (plainSelect.getWhere() != null) {
                where.add(plainSelect.getWhere());
            }

            if (plainSelect.getFromItem() instanceof Table) {
                tables.add((Table) plainSelect.getFromItem());
            }

            List<SelectItem> newSelectItems = new ArrayList<>();
            NestedSelectItemTainter selectItemTainter = new NestedSelectItemTainter(this.parameters, this.selectItemReference, this.plannedExpressions, this.tables, this.where);
            selectItemTainter.setAssignmentValues(this.assignmentValues);
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                newSelectItems.add(selectItem);
                if (selectItem.toString().toLowerCase().contains("(select")) {
                    NestedSelectItemTainter nestedSelectItemTainter = new NestedSelectItemTainter(this.parameters, this.selectItemReference, this.plannedExpressions, this.tables, this.where);
                    selectItem.accept(nestedSelectItemTainter);

                    if (selectItem instanceof SelectExpressionItem) {
                        SelectExpressionItem sei = new SelectExpressionItem(((SelectExpressionItem) selectItem).getExpression());
                        Alias al = sei.getAlias();
                        if (sei.getExpression() instanceof SubSelect) {
                            SelectBody selBody = ((SubSelect) sei.getExpression()).getSelectBody();

                        }
                    }
                } else {
                    selectItem.accept(selectItemTainter);
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
        plainSelect.getWhere().accept(new WhereExpressionTainter(this.parameters, WhereExpressionTainter.WhereExpressionKind.QUERY_SUBSELECT_WHERE));

        GroupByElement groupBy = plainSelect.getGroupBy();
        if(groupBy != null) {
            List<Expression> groupByExpressions = groupBy.getGroupByExpressions();
            List<Expression> taintedGroupByExpressions = super.taintGroupBy(groupByExpressions);
            groupBy.setGroupByExpressions(taintedGroupByExpressions);
        }
    }
}
