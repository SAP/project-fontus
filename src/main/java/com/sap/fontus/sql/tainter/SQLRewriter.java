package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLRewriter {

	private static List<String> keyWords;
	private static List<String> passThrough;

	public static void main(String[] args) {
		SQLRewriter rewriter = new SQLRewriter();

		//array of commands that should be taken into account when creating the new sql file
		String[] relevantSqlCommands = {"ALTER", "CREATE", "DELETE", "INSERT",
				"UPDATE", "WITH", "DROP", "LOCK", "UNLOCK", "SELECT"};
		keyWords = Arrays.asList(relevantSqlCommands);
		String[] passThroughCommands = {"LOCK", "UNLOCK"};
		passThrough = Arrays.asList(passThroughCommands);
		try {
			//rewriter.readFile(args[0]);
			rewriter.taintStatement("SELECT *, (SELECT name1 FROM table1 WHERE age = 5 AND id > 2) AS ag FROM table2;");
			rewriter.taintStatement("SELECT number1 AS age, (SELECT COUNT(id1) FROM table1) AS cid FROM table2;");
			rewriter.taintStatement("SELECT * FROM table1 WHERE (id1, id2) IN (SELECT number1, number2 FROM table2);");
			rewriter.taintStatement("SELECT number1 AS age, (SELECT AVG(id1) FROM table1) AS cid FROM table2;");
		} catch (Exception e) {
			System.out.println("Yeah well that didnt work.");
			e.printStackTrace();
		}
		//System.out.println("Result: " + rewriter.rewrite());

	}

	private SQLRewriter() {}

	private String taintStatement(String statement) {
		if (passThrough.stream().anyMatch(statement.substring(0, statement.indexOf(' '))::equalsIgnoreCase)) {
			return statement;
		}
		try {
			Statements stmts = CCJSqlParserUtil.parseStatements(statement);
			StatementTainter tainter = new StatementTainter();
			System.out.println("Tainting: " + statement);
			stmts.accept(tainter);
			String taintedStatement = stmts.toString();
			System.out.println("Tainted: " + taintedStatement);
			return taintedStatement;
		} catch (JSQLParserException e) {
			System.out.printf("Error parsing '%s': %s%n", statement, e);
		}
		return null;
	}

	private void readFile(String file) throws IOException {
		List<String> statements = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file)); PrintStream pr = new PrintStream("tainted_" + file)){
			String line;
			StringBuilder command = new StringBuilder(10);
			boolean inCommand = true;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("-") || line.startsWith("/")) {
					pr.println(line);
					continue;
				}
				if (inCommand) {
					command.append(line);
				} else if (keyWords.stream().anyMatch(line.substring(0, line.indexOf(' '))::equalsIgnoreCase)) {
					command.append(line);
					inCommand = true;
				}
				if (line.endsWith(";")) {
					inCommand = false;
					if (command.length() > 0) {
						pr.println(this.taintStatement(command.toString()));
						command = new StringBuilder(10);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
