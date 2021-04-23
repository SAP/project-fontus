package com.sap.fontus.sql_injection;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.*;
import com.alibaba.druid.DbType;
import com.alibaba.druid.util.JdbcConstants;
import com.sap.fontus.utils.NetworkRequestObject;
import com.sap.fontus.utils.NetworkResponseObject;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.druid.sql.SQLUtils.toSQLString;

public class SQLChecker {

    private static JSONArray getSqlInjectionInfo(List<SqlLexerToken> tokens, JSONArray taint_ranges, String sql_string){
        JSONArray injection_info_arr = new JSONArray();
        for(int i=0; i<taint_ranges.length(); i++){
            JSONObject taint_range = taint_ranges.getJSONObject(i);
            int taint_start = taint_range.getInt("start");
            int taint_end = taint_range.getInt("end");
//            String tainted_string = sql_string.substring(taint_start,taint_end);
//            if(checkOperators(tainted_string)){
//                JSONObject injection_info_obj_op = new JSONObject();
//                injection_info_obj_op.put("token_info","SQL Operator Found");
//                injection_info_obj_op.put("taint_info",taint_range);
//                injection_info_arr.put(injection_info_obj_op);
//            }
            for(SqlLexerToken token: tokens){
                if(checkBorders(token,taint_start,taint_end) || startsComment(token,taint_start,taint_end)){
                    JSONObject injection_info_obj = new JSONObject();
                    JSONObject token_info = new JSONObject();
                    token_info.put("start",token.begin);
                    token_info.put("end",token.end);
                    token_info.put("sql_token",token.token);
                    injection_info_obj.put("token_info",token_info);
                    injection_info_obj.put("taint_info",taint_range);
                    injection_info_arr.put(injection_info_obj);
                }
            }
        }
        return injection_info_arr;
    }

//    private static boolean checkOperators(String tainted_string){
//        // Check for Arithmetic, Bitwise or Comparison Operators
//        Pattern op_pattern1 = Pattern.compile("[+*/=\\-%><&|^]");
//        Matcher op_matcher1 = op_pattern1.matcher(tainted_string.toLowerCase(Locale.ROOT));
//        while(op_matcher1.find()){
//            return true;
//        }
//
//        // Check for Logical Operators
//        final List<String> sql_logical_operators = Arrays.asList("ALL","AND","ANY","BETWEEN","EXISTS","IN","LIKE","NOT",
//                "OR","SOME");
//        for(String operator : sql_logical_operators){
//            // If the operators are surrounded by spaces or at the beginning or end of string
//            Pattern op_pattern2 = Pattern.compile("(?<=\\s|^)" + operator + "(?=\\s+|$)");
//            Matcher op_matcher2 = op_pattern2.matcher(tainted_string.toLowerCase(Locale.ROOT));
//            while(op_matcher2.find()){
//                return true;
//            }
//        }
//        return false;
//    }

    private static boolean checkBorders(SqlLexerToken token,int taint_start,int taint_end){
        return taint_start < token.begin && token.begin < taint_end ||
                taint_start < token.end && token.end < taint_end;
    }

    private static boolean startsComment(SqlLexerToken token_range,int taint_start,int taint_end){
        return token_range.token_type == 1 && taint_start <= token_range.begin && taint_end >= token_range.begin + 2;
    }

    private static List<SqlLexerToken> getLexerTokens(String sql_query) {
        List<SqlLexerToken> lexer_tokens = new ArrayList<>();

        try {
            // Parse SQL Query
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql_query, DbType.mysql);
            List<SQLStatement> statementList = parser.parseStatementList();
            if(statementList.size() > 1){
                lexer_tokens.add(new SqlLexerToken(0,sql_query.length()-1,"Multiple_SQL_Queries",0));
                return lexer_tokens;
            }
            Lexer lexer = SQLParserUtils.createLexer(sql_query, DbType.mysql);
            int startPos = lexer.pos();
            lexer.nextToken();
            while(lexer.token() != Token.EOF){
                // Create the corresponding LexerToken
                lexer_tokens.add(new SqlLexerToken(startPos,lexer.pos(),lexer.token().toString(),0));
                System.out.println("tokenType : " + lexer.token() + ", startPos : " + startPos + ", endPos : " + lexer.pos());
                startPos = lexer.pos();
                lexer.nextToken();
            }
        }
        catch (ParserException e) {
            //System.out.println("sql parsing exception : " + Arrays.toString(e.getStackTrace()));
            lexer_tokens.add(new SqlLexerToken(0,sql_query.length()-1,"SQL_Parsing_Error",0));
            return lexer_tokens;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        // Check for comment injection
        Pattern comment_pattern1 = Pattern.compile("(?:/\\*)+");
        Matcher matcher1 = comment_pattern1.matcher(sql_query.toLowerCase(Locale.ROOT));
        while(matcher1.find()){
            lexer_tokens.add(new SqlLexerToken(matcher1.start(),matcher1.end(),"/*",1));
        }
        Pattern comment_pattern2 = Pattern.compile("[#]+");
        Matcher matcher2 = comment_pattern2.matcher(sql_query.toLowerCase(Locale.ROOT));
        while(matcher2.find()){
            lexer_tokens.add(new SqlLexerToken(matcher2.start(),matcher2.end(),"#",1));
        }
        Pattern comment_pattern3 = Pattern.compile("[-]{2,}");
        Matcher matcher3 = comment_pattern3.matcher(sql_query.toLowerCase(Locale.ROOT));
        while(matcher3.find()){
            lexer_tokens.add(new SqlLexerToken(matcher3.start(),matcher3.end(),"--",1));
        }
        return lexer_tokens;
    }



    public static void checkTaintedString(String tainted_string) throws RuntimeException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InterruptedException {
        JSONObject json_obj = new JSONObject(tainted_string);
        List<SqlLexerToken> token_ranges = getLexerTokens(json_obj.getString("payload"));
        JSONArray taint_ranges = json_obj.getJSONArray("ranges");
        String sql_string = json_obj.getString("payload");
        //JSONArray json_array = antiSQLInjection.getSqlInjectionInfo(sql_string);
        JSONArray json_array = getSqlInjectionInfo(token_ranges,taint_ranges,sql_string);
        System.out.println(json_array.toString());
        NetworkResponseObject.setResponseMessage(new NetworkRequestObject(),!json_array.isEmpty());
    }

    public static void logTaintedString(String tainted_string) throws RuntimeException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InterruptedException {
        JSONObject json_obj = new JSONObject(tainted_string);
        List<SqlLexerToken> token_ranges = getLexerTokens(json_obj.getString("payload"));
        JSONArray taint_ranges = json_obj.getJSONArray("ranges");
        String sql_string = json_obj.getString("payload");
        JSONArray json_array = getSqlInjectionInfo(token_ranges,taint_ranges,sql_string);
        System.out.println(json_array.toString());
        if(!json_array.isEmpty()){
            Logger logger = Logger.getLogger("SqlInjectionLog");
            FileHandler fh;

            try {

                // This block configure the logger with handler and formatter
                fh = new FileHandler("sql_injection_logger.log", true);
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);

                // the following statement is used to log any messages
                logger.info(json_array.toString());

            } catch (SecurityException | IOException e) {
                e.printStackTrace();
            }
            throw new InterruptedException("SQL Injection Error");
        }
    }

    public static void main(String[] args) {
        //String sqlString = "insert into table1 values      (data1, data2)";
        String sqlString = "select * from table1 where id = 3";
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

//    public static void main(String[] args) throws SqlParseException {
//        //String sqlString = "select * from emp,der where id=2 and 1=1";
//        String sqlString = "insert into emp values(1,2)";
//        SqlParser parser = SqlParser.create(sqlString);
//        SqlNode node = parser.parseStmt();
//        System.out.println(node);
//        SqlInsert node_s = (SqlInsert) node;
//        System.out.println(node_s.getOperandList());
//        String m = node.toString();
//        String[] tokens = node.toString().split("\\r?\\n");
//
//        for(String token : tokens){
//            //Pattern pattern = Pattern.compile("(?<=\\s|^|[;])" + token.toLowerCase(Locale.ROOT) +"(?=\\s|$)");
//            String token_string = token.toLowerCase(Locale.ROOT).replace("`","");
//            Pattern pattern = Pattern.compile(token_string);
//            System.out.println(token_string);
//            Matcher matcher = pattern.matcher(sqlString.toLowerCase(Locale.ROOT));
//            while(matcher.find()){
//                System.out.println(matcher.start() + " " + matcher.end());
//            }
//        }
//
//        System.out.println(Arrays.toString(tokens));
//        FontusSqlVisitor f_visitor = new FontusSqlVisitor();
//        SqlParserPos x = (SqlParserPos) node.accept(f_visitor);
//        System.out.println("x = " + x);
//    }




