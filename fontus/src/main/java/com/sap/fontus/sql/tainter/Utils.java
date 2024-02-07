package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sap.fontus.Constants.TAINT_PREFIX;

public final class Utils {

    private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"", Pattern.LITERAL);
    private static final Pattern BACKTICKS = Pattern.compile("`", Pattern.LITERAL);

    private Utils() {}

    public static String taintColumnName(CharSequence name) {
        return "`" + TAINT_PREFIX + BACKTICKS.matcher(DOUBLE_QUOTES.matcher(name).replaceAll(Matcher.quoteReplacement(""))).replaceAll(Matcher.quoteReplacement("")) + "`";
    }

    static Column getTaintColumn(Table table, Column column) {
        String taintColumnName = taintColumnName(column.getColumnName());
        return new Column(table + "." + taintColumnName);
    }
    static Column getTaintColumn(Column column) {
        return new Column(taintColumnName(column.getColumnName()));
    }

    static List<String> taintColumnNames(Iterable<String> columnNames) {
        List<String> newColumnNames = new ArrayList<>();
        for (String columnName : columnNames) {
            newColumnNames.add(columnName);
            newColumnNames.add(taintColumnName(columnName));
        }
        return newColumnNames;
    }

    static List<ColumnDefinition> taintColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        List<ColumnDefinition> newColumnDefinitions = new ArrayList<>();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            newColumnDefinitions.add(columnDefinition);
            ColumnDefinition newColumnDefinition = new ColumnDefinition();
            newColumnDefinition.setColumnName(taintColumnName(columnDefinition.getColumnName()));
            String dataType = columnDefinition.getColDataType().getDataType().toUpperCase();
            if (dataType.contains("TEXT")) {
                newColumnDefinition.setColDataType(columnDefinition.getColDataType());
            } else {
                ColDataType colDataType = new ColDataType();
                colDataType.setDataType("TEXT");
                newColumnDefinition.setColDataType(colDataType);
            }
            newColumnDefinitions.add(newColumnDefinition);
        }
        return newColumnDefinitions;
    }

    /**
     * For consumption by tools related to Fontus.
     *
     * This is factored out here, as it is very easy to get super confusing error messages iff one depends on the shadow
     * jar of the fontus module and have the JSQLParser stuff as a dependency yourself.
     * @param query original query
     * @return tainted query
     */
    public static String taintSqlStatement(String query)  {
        try {
            StatementTainter tainter = new StatementTainter();
            Statements stmts = CCJSqlParserUtil.parseStatements(query);
            stmts.accept(tainter);
            return stmts.toString().trim();
        } catch(JSQLParserException ex) {
            throw new RuntimeException(ex);
        }
    }
}
