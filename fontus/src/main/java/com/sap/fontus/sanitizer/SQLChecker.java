package com.sap.fontus.sanitizer;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.*;
import com.sap.fontus.config.abort.AbortObject;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.utils.NetworkRequestObject;
import com.sap.fontus.utils.NetworkResponseObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SQLChecker {

    // List of tokens which are allowed to be tainted
    // Prevents keywords being tainted
    private static final Token[] taintableTokens = {
            Token.IDENTIFIER,
            Token.LITERAL_INT,
            Token.LITERAL_FLOAT,
            Token.LITERAL_HEX,
            Token.LITERAL_CHARS,
            Token.LITERAL_NCHARS,
            Token.LITERAL_PATH,
            Token.LITERAL_ALIAS
    };

    private static List<SqlTokenOverlap> getSqlInjectionInfo(List<SqlLexerToken> tokens, IASTaintRanges taintRanges, int addlPos, Token[] allowedTokens) {
        List<SqlTokenOverlap> overlaps = new ArrayList<>();
        for (IASTaintRange taintRange : taintRanges) {
            int taintStart = addlPos + taintRange.getStart();
            int taintEnd = addlPos + taintRange.getEnd();
            for (SqlLexerToken token : tokens) {
                if (checkBorders(token, taintStart, taintEnd) || (token.tokenType == 1)) {
                    // First check if the tainted string tries to change the SQL syntax
                    SqlTokenOverlap overlap = new SqlTokenOverlap(token, taintRange);
                    System.out.println("Overlap:" + overlap);
                    overlaps.add(overlap);
                } else if (checkContained(token, taintStart, taintEnd)) {
                    // If part or all of the token is tainted, check if it is in the allow list:
                    if (!Arrays.asList(allowedTokens).contains(token.getToken())) {
                        SqlTokenOverlap overlap = new SqlTokenOverlap(token, taintRange);
                        System.out.println("Contained:" + overlap);
                        overlaps.add(overlap);
                    }
                }
            }
        }
        return overlaps;
    }

    private static List<SqlTokenOverlap> getSqlInjectionInfo(List<SqlLexerToken> tokens, IASTaintRanges taintRanges, int addlPos) {
        return getSqlInjectionInfo(tokens, taintRanges, addlPos, taintableTokens);
    }

    private static boolean checkBorders(SqlLexerToken token, int taintStart, int taintEnd){
        return taintStart < token.begin && token.begin < taintEnd ||
                taintStart < token.end && token.end < taintEnd;
    }

    private static boolean checkContained(SqlLexerToken token, int taintStart, int taintEnd){
        return taintStart >= token.begin && taintStart < token.end ||
                taintEnd > token.begin && taintEnd <= token.end;
    }

    private static List<SqlLexerToken> getLexerTokens(String sqlQuery) {
        System.out.println("SQL Query : " + sqlQuery);
        List<SqlLexerToken> lexerTokens = new ArrayList<>();
        DbType dbType = DbType.postgresql;

        try {
            // Parse SQL Query
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sqlQuery, dbType);
            List<SQLStatement> statementList = parser.parseStatementList();
            if(statementList.size() > 1){
                lexerTokens.add(new SqlLexerToken(0,sqlQuery.length()-1,Token.EOF,1));
                return lexerTokens;
            }
            Lexer lexer = SQLParserUtils.createLexer(sqlQuery, dbType);
            int startPos = lexer.pos();
            lexer.nextToken();
            while(lexer.token() != Token.EOF){
                // Create the corresponding LexerToken
                lexerTokens.add(new SqlLexerToken(startPos,lexer.pos(),lexer.token(),0));
                System.out.println("tokenType : " + lexer.token() + ", startPos : " + startPos + ", endPos : " + lexer.pos());
                startPos = lexer.pos();
                lexer.nextToken();
            }
//            if(startPos != sqlQuery.length()){
//                lexerTokens.add(new SqlLexerToken(startPos,sqlQuery.length(),"Unparsed_Segment",0,true));
//            }
        }
        catch (ParserException e) {
            System.out.println("SQL Parsing Exception");
            lexerTokens.add(new SqlLexerToken(0,sqlQuery.length()-1,Token.ERROR,1));
            //return lexerTokens;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return lexerTokens;
    }

    public static boolean sqlInjectionDetected(String sqlString, IASTaintRanges taintRanges) {
        List<SqlLexerToken> tokenRanges = getLexerTokens(sqlString);
        List<SqlTokenOverlap> overlaps = getSqlInjectionInfo(tokenRanges,taintRanges,0);
        return !overlaps.isEmpty();
    }

    public static boolean sqlInjectionDetected(String sqlString, IASTaintRanges taintRanges, Token[] allowedTokens) {
        List<SqlLexerToken> tokenRanges = getLexerTokens(sqlString);
        List<SqlTokenOverlap> overlaps = getSqlInjectionInfo(tokenRanges,taintRanges,0, allowedTokens);
        return !overlaps.isEmpty();
    }

    public static void reportTaintedString(AbortObject abortObject) {
        String sqlString = abortObject.getPayload();
        String sink = abortObject.getSinkFunction();
        int addlPos = 0;

        IASTaintRanges taintRanges = abortObject.getRanges();

        // Sql String template (temporary fix) if parameters are bound to the original string
        if("java/sql/PreparedStatement.setString(ILjava/lang/String;)V".equals(sink)) {
            class SQLElement{
                public final String sqlStatement;
                public final int startPos;

                public SQLElement(String sqlStatement, int startPos){
                    this.sqlStatement = sqlStatement;
                    this.startPos = startPos;
                }
            }
            List<SQLElement> sqlElementList = new ArrayList<>();
            sqlElementList.add(new SQLElement("SELECT * FROM table1 WHERE username = '" + sqlString + "'", 39));
            sqlElementList.add(new SQLElement("SELECT * FROM table1 WHERE username = \"" + sqlString + "\"", 39));

            boolean sqlInjectionPresent = false;

            for(SQLElement sqlElement : sqlElementList) {
                List<SqlLexerToken> tokenRanges = getLexerTokens(sqlElement.sqlStatement);
                List<SqlTokenOverlap> injInfoArray = getSqlInjectionInfo(tokenRanges,taintRanges,sqlElement.startPos);
                System.out.println("checkTaintedString : " + injInfoArray);
                if(!injInfoArray.isEmpty()){
                    sqlInjectionPresent = true;
                }
            }
            NetworkResponseObject.setResponseMessage(new NetworkRequestObject(), sqlInjectionPresent);
        } else if ("java/sql/PreparedStatement.setInt(II)V".equals(sink)) {
            String sqlStmt = "SELECT * FROM table1 WHERE id = " + sqlString;
            List<SqlLexerToken> tokenRanges = getLexerTokens(sqlStmt);

            List<SqlTokenOverlap> injInfoArray = getSqlInjectionInfo(tokenRanges,taintRanges,32);
            System.out.println("checkTaintedString : " + injInfoArray);
            NetworkResponseObject.setResponseMessage(new NetworkRequestObject(), !injInfoArray.isEmpty());
        } else {
            List<SqlLexerToken> tokenRanges = getLexerTokens(sqlString);
            List<SqlTokenOverlap> injInfoArray = getSqlInjectionInfo(tokenRanges,taintRanges,addlPos);
            System.out.println("checkTaintedString : " + injInfoArray);
            NetworkResponseObject.setResponseMessage(new NetworkRequestObject(), !injInfoArray.isEmpty());
        }
    }

    public static void logTaintedString(AbortObject abortObject) {
        String sqlString = abortObject.getPayload();
        String sink = abortObject.getSinkFunction();
        int addlPos = 0;

        // Sql String template (temporary fix) if parameters are bound to the original string
        if("java/sql/PreparedStatement.setString(ILjava/lang/String;)V".equals(sink)){
            sqlString = "SELECT * FROM table1 WHERE username = '" + sqlString + "'";
            addlPos = 39;
        }

        // Sql Int template (temporary fix) if parameters are bound to the original string
        if("java/sql/PreparedStatement.setInt(II)V".equals(sink)){
            sqlString = "SELECT * FROM table1 WHERE id = " + sqlString;
            addlPos = 32;
        }

        List<SqlLexerToken> tokenRanges = getLexerTokens(sqlString);
        IASTaintRanges taintRanges = abortObject.getRanges();

        List<SqlTokenOverlap> jsonArray = getSqlInjectionInfo(tokenRanges, taintRanges, addlPos);
        System.out.println("logTaintedString : " + jsonArray);
        if(!jsonArray.isEmpty()){
            Logger logger = Logger.getLogger("SqlInjectionLog");
            FileHandler fh;
            try {
                // This block configures the logger with handler and formatter
                fh = new FileHandler("sql_injection_logger.log", true);
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);

                // the following statement is used to log any messages
                logger.info(jsonArray.toString());

            } catch (SecurityException | IOException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("Logging: SQL Injection Error");
        }
    }

    public static void main(String[] args) {
        //String sqlString = "insert into table1 values      (data1, data2)";
        String sqlString = "SELECT * FROM student_data where student_name = 1";
//        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sqlString, DbType.mysql);
//        parser.parseStatementList();
//
//        Lexer lexer = SQLParserUtils.createLexer(sqlString, DbType.mysql);
//        int startPos = lexer.pos();
//        lexer.nextToken();
//        while(lexer.token() != Token.EOF){
//            System.out.println("tokenType : " + lexer.token() + ", startPos : " + startPos + ", endPos : " + lexer.pos());
//            startPos = lexer.pos();
//            lexer.nextToken();
//        }
        getLexerTokens(sqlString);
    }

}

//    private static boolean hasComment(String sql_stmt,SqlLexerToken token){
//        if(token.token.equals("LITERAL_CHARS") || token.token.equals("LITERAL_ALIAS")){
//            return false;
//        }
//        String token_string = sql_stmt.substring(token.begin, token.end);
//
//        // Check for comment presence
//        Pattern comment_pattern1 = Pattern.compile("(?:/\\*)+");
//        Matcher matcher1 = comment_pattern1.matcher(token_string.toLowerCase(Locale.ROOT));
//
//        Pattern comment_pattern2 = Pattern.compile("[#]+");
//        Matcher matcher2 = comment_pattern2.matcher(token_string.toLowerCase(Locale.ROOT));
//
//        Pattern comment_pattern3 = Pattern.compile("[-]{2,}");
//        Matcher matcher3 = comment_pattern3.matcher(token_string.toLowerCase(Locale.ROOT));
//        return matcher1.find() || matcher2.find() || matcher3.find();
//    }

//    private static boolean startsComment(SqlLexerToken token_range,int taint_start,int taint_end){
//        return token_range.token_type == 1 && taint_start <= token_range.begin && taint_end >= token_range.begin + 2;
//    }
