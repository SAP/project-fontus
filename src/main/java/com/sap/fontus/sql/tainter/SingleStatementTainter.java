package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

import java.util.ArrayList;
import java.util.Map;

public class SingleStatementTainter {
    public static void main(String[] args) {
        String statement = args[0];
        Statements stmts = null;
        try {
            stmts = CCJSqlParserUtil.parseStatements(statement);
        } catch (JSQLParserException e) {
            System.out.printf("Error parsing '%s': %s%n", statement, e);
        }
        StatementTainter tainter = new StatementTainter();
        System.out.println("Tainting: " + statement);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        System.out.println("Tainted: " + taintedStatement);
        Map<Integer, Integer> indices = tainter.getIndices();
        Map<String, String> infos = tainter.getAssignmentInfos();
        for(Map.Entry<Integer, Integer> entry : indices.entrySet()) {
            System.out.printf("%d: %d%n", entry.getKey(), entry.getValue());
        }
        for(Map.Entry<String, String> entry : infos.entrySet()) {
            System.out.printf("%s: %s%n", entry.getKey(), entry.getValue());
        }
    }
}
