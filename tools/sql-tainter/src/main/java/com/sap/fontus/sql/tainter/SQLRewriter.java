package com.sap.fontus.sql.tainter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SQLRewriter {

	//array of commands that should be taken into account when creating the new sql file
	private final List<String> keyWords = Arrays.asList("ALTER", "CREATE", "DELETE", "INSERT",
			"UPDATE", "WITH", "DROP", "LOCK", "UNLOCK", "SELECT");
	private final List<String> passThrough = Arrays.asList("LOCK", "UNLOCK");

	public static void main(String[] args) {
		SQLRewriter rewriter = new SQLRewriter();

		try {
			rewriter.readFile(args[0]);
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
			System.out.printf("Tainting: '%s'%n", statement);
			String taintedStatement = Utils.taintSqlStatement(statement);
			System.out.printf("Tainted: '%s'%n", taintedStatement);
			return taintedStatement;
		} catch (Exception e) {
			System.out.printf("Error parsing '%s': %s%n", statement, e);
		}
		return null;
	}

	private void readFile(String file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8)); PrintStream pr = new PrintStream("tainted_" + file)){
			String line;
			StringBuilder command = new StringBuilder(10);
			boolean inCommand = true;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("-") || line.startsWith("/")) {
					pr.println(line);
					continue;
				}
				if (inCommand) {
					// Add whitespace to ensure the removal of line breaks we implicitly do here does not mess up statements
					command.append(line).append(" ");
				} else if (keyWords.stream().anyMatch(line.substring(0, line.indexOf(' '))::equalsIgnoreCase)) {
					command.append(line);
					inCommand = true;
				}
				if (line.stripTrailing().endsWith(";")) {
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
