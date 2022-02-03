package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

import java.util.ArrayList;

public class SingleStatementTainter {
    public static void main(String[] args) {
        String statement = args[0];
        Statements stmts = null;
        try {
            stmts = CCJSqlParserUtil.parseStatements(statement);
        } catch (JSQLParserException e) {
            System.out.printf("Error parsing '%s': %s%n", statement, e);
        }
        StatementTainter tainter = new StatementTainter(new ArrayList<>());
        System.out.println("Tainting: " + statement);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        System.out.println("Tainted: " + taintedStatement);
    }
}
