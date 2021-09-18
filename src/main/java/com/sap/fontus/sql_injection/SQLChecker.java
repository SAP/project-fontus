package com.sap.fontus.sql_injection;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.*;
import com.alibaba.druid.DbType;
import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.utils.NetworkRequestObject;
import com.sap.fontus.utils.NetworkResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLChecker {

    private static JSONArray getSqlInjectionInfo(List<SqlLexerToken> tokens, JSONArray taint_ranges, int addl_pos){
        JSONArray injection_info_arr = new JSONArray();
        for(int i=0; i<taint_ranges.length(); i++){
            JSONObject taint_range = taint_ranges.getJSONObject(i);
            int taint_start = addl_pos + taint_range.getInt("start");
            int taint_end = addl_pos + taint_range.getInt("end");
            for(SqlLexerToken token: tokens){
                if(checkBorders(token,taint_start,taint_end) || (token.token_type == 1)){
                    JSONObject injection_info_obj = new JSONObject();
                    JSONObject token_info = new JSONObject();
                    token_info.put("start",token.begin);
                    token_info.put("end",token.end);
                    token_info.put("sql_token",token.token);
                    token_info.put("sql_token_type",token.token_type);
                    injection_info_obj.put("token_info",token_info);
                    injection_info_obj.put("taint_info",taint_range);
                    injection_info_arr.put(injection_info_obj);
                    if(token.has_comment){
                        JSONObject injection_info_obj2 = new JSONObject();
                        JSONObject token_info2 = new JSONObject();
                        token_info2.put("start",token.begin);
                        token_info2.put("end",token.end);
                        token_info2.put("sql_token","Comment");
                        token_info2.put("sql_token_type",-1);
                        injection_info_obj2.put("token_info",token_info2);
                        injection_info_obj2.put("taint_info",taint_range);
                        injection_info_arr.put(injection_info_obj2);
                    }
                }
            }
        }
        return injection_info_arr;
    }

    private static boolean checkBorders(SqlLexerToken token,int taint_start,int taint_end){
        return taint_start < token.begin && token.begin < taint_end ||
                taint_start < token.end && token.end < taint_end;
    }

    private static List<SqlLexerToken> getLexerTokens(String sql_query) {
        System.out.println("SQL Query : " + sql_query);
        List<SqlLexerToken> lexer_tokens = new ArrayList<>();
        DbType db_type = DbType.mysql;

        try {
            // Parse SQL Query
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql_query, db_type);
            List<SQLStatement> statementList = parser.parseStatementList();
            if(statementList.size() > 1){
                lexer_tokens.add(new SqlLexerToken(0,sql_query.length()-1,"Multiple_SQL_Queries",1,false));
                return lexer_tokens;
            }
            Lexer lexer = SQLParserUtils.createLexer(sql_query, db_type);
            int startPos = lexer.pos();
            lexer.nextToken();
            while(lexer.token() != Token.EOF){
                // Create the corresponding LexerToken
                lexer_tokens.add(new SqlLexerToken(startPos,lexer.pos(),lexer.token().toString(),0, lexer.hasComment()));
                System.out.println("tokenType : " + lexer.token() + ", startPos : " + startPos + ", endPos : " + lexer.pos());
                startPos = lexer.pos();
                lexer.nextToken();
            }
            if(startPos != sql_query.length()){
                lexer_tokens.add(new SqlLexerToken(startPos,sql_query.length(),"Unparsed_Segment",0,true));
            }
        }
        catch (ParserException e) {
            System.out.println("SQL Parsing Exception");
            lexer_tokens.add(new SqlLexerToken(0,sql_query.length()-1,"SQL_Parsing_Error",1,false));
            //return lexer_tokens;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return lexer_tokens;
    }

    public static void reportTaintedString(String tainted_string) throws RuntimeException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InterruptedException {
        JSONObject json_obj = new JSONObject(tainted_string);
        String sql_string = json_obj.getString("payload");
        int addl_pos = 0;

        // Sql String template (temporary fix) if parameters are bound to the original string
        if(json_obj.getString("sink").equals("java/sql/PreparedStatement.setString(ILjava/lang/String;)V")){
            class SQLElement{
                public String sql_statement;
                public int start_pos;

                public SQLElement(String sql_statement, int start_pos){
                    this.sql_statement = sql_statement;
                    this.start_pos = start_pos;
                }
            }
            List<SQLElement> sql_element_list = new ArrayList<>();
            sql_element_list.add(new SQLElement("SELECT * FROM table1 WHERE username = '" + sql_string + "'", 39));
            sql_element_list.add(new SQLElement("SELECT * FROM table1 WHERE username = \"" + sql_string + "\"", 39));

            boolean sql_injection_present = false;

            for(SQLElement sql_element : sql_element_list){
                List<SqlLexerToken> token_ranges = getLexerTokens(sql_element.sql_statement);
                JSONArray taint_ranges = json_obj.getJSONArray("ranges");
                JSONArray inj_info_array = getSqlInjectionInfo(token_ranges,taint_ranges,sql_element.start_pos);
                System.out.println("checkTaintedString : " + inj_info_array.toString());
                if(!inj_info_array.isEmpty()){
                    sql_injection_present = true;
                }
            }
            NetworkResponseObject.setResponseMessage(new NetworkRequestObject(),sql_injection_present);
        }
        else if(json_obj.getString("sink").equals("java/sql/PreparedStatement.setInt(II)V")){
            String sql_stmt = "SELECT * FROM table1 WHERE id = " + sql_string;
            List<SqlLexerToken> token_ranges = getLexerTokens(sql_stmt);
            JSONArray taint_ranges = json_obj.getJSONArray("ranges");

            JSONArray json_array = getSqlInjectionInfo(token_ranges,taint_ranges,32);
            System.out.println("checkTaintedString : " + json_array.toString());
            NetworkResponseObject.setResponseMessage(new NetworkRequestObject(),!json_array.isEmpty());
        }
        else{
            List<SqlLexerToken> token_ranges = getLexerTokens(sql_string);
            JSONArray taint_ranges = json_obj.getJSONArray("ranges");

            JSONArray json_array = getSqlInjectionInfo(token_ranges,taint_ranges,addl_pos);
            System.out.println("checkTaintedString : " + json_array.toString());
            NetworkResponseObject.setResponseMessage(new NetworkRequestObject(),!json_array.isEmpty());
        }
    }

    public static void logTaintedString(String tainted_string) throws RuntimeException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InterruptedException {
        JSONObject json_obj = new JSONObject(tainted_string);
        String sql_string = json_obj.getString("payload");
        int addl_pos = 0;

        // Sql String template (temporary fix) if parameters are bound to the original string
        if(json_obj.getString("sink").equals("java/sql/PreparedStatement.setString(ILjava/lang/String;)V")){
            sql_string = "SELECT * FROM table1 WHERE username = '" + sql_string + "'";
            addl_pos = 39;
        }

        // Sql Int template (temporary fix) if parameters are bound to the original string
        if(json_obj.getString("sink").equals("java/sql/PreparedStatement.setInt(II)V")){
            sql_string = "SELECT * FROM table1 WHERE id = " + sql_string + "";
            addl_pos = 32;
        }

        List<SqlLexerToken> token_ranges = getLexerTokens(sql_string);
        JSONArray taint_ranges = json_obj.getJSONArray("ranges");

        JSONArray json_array = getSqlInjectionInfo(token_ranges,taint_ranges,addl_pos);
        System.out.println("logTaintedString : " + json_array.toString());
        if(!json_array.isEmpty()){
            Logger logger = Logger.getLogger("SqlInjectionLog");
            FileHandler fh;
            try {

                // This block configures the logger with handler and formatter
                fh = new FileHandler("sql_injection_logger.log", true);
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);

                // the following statement is used to log any messages
                logger.info(json_array.toString());

            } catch (SecurityException | IOException e) {
                e.printStackTrace();
            }
            // throw new InterruptedException("SQL Injection Error");
        }
    }

    public static void main(String[] args) {
        //String sqlString = "insert into table1 values      (data1, data2)";
        String sqlString = "SELECT * FROM table1 WHERE username = \"<STYLE>BODY{-moz-binding:url(\"http://ha.ckers.org/xssmoz.xml#xss\")}</STYLE>\"";
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
