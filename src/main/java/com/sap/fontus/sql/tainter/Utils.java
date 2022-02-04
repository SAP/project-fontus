package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sap.fontus.Constants.TAINT_PREFIX;

final class Utils {

    private static final Pattern DOUBLE_QUOTES = Pattern.compile("\"", Pattern.LITERAL);
    private static final Pattern BACKTICKS = Pattern.compile("`", Pattern.LITERAL);

    private Utils() {}

    static String taintColumnName(CharSequence name) {
        return "`" + TAINT_PREFIX + BACKTICKS.matcher(DOUBLE_QUOTES.matcher(name).replaceAll(Matcher.quoteReplacement(""))).replaceAll(Matcher.quoteReplacement("")) + "`";
    }

    static Column getTaintColumn(Table table, Column column) {
        String taintColumnName = taintColumnName(column.getColumnName());
        return new Column(table + "." + taintColumnName);
    }
    static Column getTaintColumn(Column column) {
        return new Column(taintColumnName(column.getColumnName()));
    }
}
