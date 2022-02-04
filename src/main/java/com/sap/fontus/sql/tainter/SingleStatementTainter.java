package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

public class SingleStatementTainter {
    public static void main(String[] args) {
        String statement = args[0];
        try {
            Statements stmts = CCJSqlParserUtil.parseStatements(statement);
            StatementTainter tainter = new StatementTainter();
            System.out.println("Tainting: " + statement);
            stmts.accept(tainter);
            String taintedStatement = stmts.toString();
            System.out.println("Tainted: " + taintedStatement);
        } catch (JSQLParserException e) {
            System.out.printf("Error parsing '%s': %s%n", statement, e);
        }

    }
}
