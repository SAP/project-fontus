package com.sap.fontus.sql.tainter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLRewriter {

	private List<Taint> taints;
	private String sqlString;
	private static List<String> keyWords;

	public static void main(String[] args) {
		SQLRewriter rewriter = new SQLRewriter();

		//array of commands that should be taken into account when creating the new sql file
		String [] relevantSqlCommands = {"ALTER","CREATE","DELETE","INSERT",
				"UPDATE","WITH","DROP","LOCK","UNLOCK"};
		keyWords = Arrays.asList(relevantSqlCommands);

		try {
			rewriter.getInput(args[0]);
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

	private void getInput() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			sqlString = br.readLine();
			sqlString = unescape(sqlString);
			String taintedValue, taintCodes;
			while ((taintedValue = br.readLine()) != null) {
				taintedValue = unescape(taintedValue);
				if ((taintCodes = br.readLine()) != null)
					taints.add(new Taint(taintedValue, taintCodes));
				else
					taints.add(new Taint(taintedValue, ""));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getInput(String file) throws IOException {
		BufferedReader br = null;
		PrintStream pr = new PrintStream("tainted_" + file);
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			String line = null;
			while((line = br.readLine()) != null ){
				if(!(line.isEmpty() ||line.charAt(0)=='-' || line.charAt(0) == '/')) {
					StringBuilder build = new StringBuilder();
					if (keyWords.contains(line.substring(0, line.indexOf(' ')))) {
						build.append(line);
					}
					while (!line.endsWith(";")) {
						line = br.readLine();
						//you may need to change this if the collate type varies
						line = line.replace("COLLATE=utf8mb4_0900_ai_ci","");
						build.append(line);
					}
					Statements stmts;
					try {
						stmts = CCJSqlParserUtil.parseStatements(build.toString());
						StatementTainter tainter = new StatementTainter(taints);
						System.out.println("Tainting: " + build.toString());
						stmts.accept(tainter);
						pr.println(stmts.toString());
					} catch (JSQLParserException e) {
						System.err.println(e.getMessage() + "\n" + e.getStackTrace() + "\n\n" + build.toString());
						//pr.println(build.toString());
					}
				}else if(!line.isEmpty()&&line.charAt(0)!='-'){
					pr.println(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String unescape(String str) {
		return str.replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
	}
	
	private String rewrite(){
		Statements stmts;
		try {
			stmts = CCJSqlParserUtil.parseStatements(sqlString);
			StatementTainter tainter = new StatementTainter(taints);
			stmts.accept(tainter);
		} catch (JSQLParserException e) {
			throw new RuntimeException(e);
		}
		return stmts.toString();
	}
}
