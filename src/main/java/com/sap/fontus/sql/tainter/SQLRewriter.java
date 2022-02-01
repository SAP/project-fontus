package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SQLRewriter {

	private List<Taint> taints;
	private String sqlString;
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
			rewriter.taintStatement("SELECT *, (SELECT name1 FROM table1 WHERE age = 5) AS ag FROM table2;");
			rewriter.taintStatement("SELECT number1 AS age, (SELECT COUNT(id1) FROM table1) AS cid FROM table2;");
			rewriter.taintStatement("SELECT * FROM table1 WHERE (id1, id2) IN (SELECT number1, number2 FROM table2);");
			rewriter.taintStatement("SELECT number1 AS age, (SELECT AVG(id1) FROM table1) AS cid FROM table2;");
		} catch (Exception e) {
			System.out.println("Yeah well that didnt work.");
			e.printStackTrace();
		}
		//System.out.println("Result: " + rewriter.rewrite());

	}

	private SQLRewriter() {
		taints = new ArrayList<>();
		sqlString = "";
	}

	private String taintStatement(String statement) {
		if (passThrough.stream().anyMatch(statement.substring(0, statement.indexOf(' '))::equalsIgnoreCase)) {
			return statement;
		}
		Statements stmts = null;
		try {
			stmts = CCJSqlParserUtil.parseStatements(statement);
		} catch (JSQLParserException e) {
			System.out.printf("Error parsing '%s': %s%n", statement, e);
		}
		StatementTainter tainter = new StatementTainter(this.taints);
		System.out.println("Tainting: " + statement);
		stmts.accept(tainter);
		String taintedStatement = stmts.toString();
		System.out.println("Tainted: " + taintedStatement);
		//Map<Integer, Integer> map = tainter.getIndices();
		//map.forEach((k,v) -> System.out.println("key: "+k+" value:"+v));
		return taintedStatement;
	}

	private void readFile(String file) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		List<String> statements = new ArrayList<>();
		try {
			PrintStream pr = new PrintStream("tainted_" + file);
			String line;
			StringBuilder command = new StringBuilder();
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
						command = new StringBuilder();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
