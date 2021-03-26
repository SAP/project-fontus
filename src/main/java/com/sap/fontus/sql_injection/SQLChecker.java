package com.sap.fontus.sql_injection;

import com.sap.fontus.sql_injection.antiSQLInjection.antiSQLInjection;
import com.sap.fontus.utils.NetworkRequestObject;
import com.sap.fontus.utils.NetworkResponseObject;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLChecker {

    private static JSONArray getSqlInjectionInfo(List<SqlLexerToken> token_ranges, JSONArray taint_ranges, String sql_string){
        JSONArray injection_info_arr = new JSONArray();
        for(int i=0; i<taint_ranges.length(); i++){
            JSONObject taint_range = taint_ranges.getJSONObject(i);
            int taint_start = taint_range.getInt("start");
            int taint_end = taint_range.getInt("end");
            String tainted_string = sql_string.substring(taint_start,taint_end);
            if(checkOperators(tainted_string)){
                JSONObject injection_info_obj_op = new JSONObject();
                injection_info_obj_op.put("token_info","SQL Operator Found");
                injection_info_obj_op.put("taint_info",taint_range);
                injection_info_arr.put(injection_info_obj_op);
            }
            for(SqlLexerToken token_range : token_ranges){
                if(checkBorders(token_range,taint_start,taint_end) || startsComment(token_range,taint_start,taint_end)){
                    JSONObject injection_info_obj = new JSONObject();
                    JSONObject token_info = new JSONObject();
                    token_info.put("start",token_range.begin);
                    token_info.put("end",token_range.end);
                    token_info.put("sql_token",token_range.token);
                    injection_info_obj.put("token_info",token_info);
                    injection_info_obj.put("taint_info",taint_range);
                    injection_info_arr.put(injection_info_obj);
                }
            }
        }
        return injection_info_arr;
    }

    private static boolean checkOperators(String tainted_string){
        // Check for Arithmetic, Bitwise or Comparison Operators
        Pattern op_pattern1 = Pattern.compile("[+*/=\\-%><&|^]");
        Matcher op_matcher1 = op_pattern1.matcher(tainted_string.toLowerCase(Locale.ROOT));
        while(op_matcher1.find()){
            return true;
        }

        // Check for Logical Operators
        final List<String> sql_logical_operators = Arrays.asList("ALL","AND","ANY","BETWEEN","EXISTS","IN","LIKE","NOT",
                "OR","SOME");
        for(String operator : sql_logical_operators){
            // If the operators are surrounded by spaces or at the beginning or end of string
            Pattern op_pattern2 = Pattern.compile("(?<=\\s|^)" + operator + "(?=\\s+|$)");
            Matcher op_matcher2 = op_pattern2.matcher(tainted_string.toLowerCase(Locale.ROOT));
            while(op_matcher2.find()){
                return true;
            }
        }
        return false;
    }

    private static boolean checkBorders(SqlLexerToken token_range,int taint_start,int taint_end){
        return taint_start < token_range.begin && token_range.begin < taint_end ||
                taint_start < token_range.end && token_range.end < taint_end;
    }

    private static boolean startsComment(SqlLexerToken token_range,int taint_start,int taint_end){
        return token_range.token_type == 1 && taint_start <= token_range.begin && taint_end >= token_range.begin + 2;
    }

    private static List<SqlLexerToken> getLexerTokens(String sql_query) {
        //final List<String> sql_tokens = Arrays.asList("ABS","ABSOLUTE","ACTION","ADA","ADD","ADMIN","AFTER","ALL","ALLOCATE","ALLOW","ALTER","ALWAYS","AND","ANY","APPLY","ARE","ARRAY","ARRAY_MAX_CARDINALITY","AS","ASC","ASENSITIVE","ASSERTION","ASSIGNMENT","ASYMMETRIC","AT","ATOMIC","ATTRIBUTE","ATTRIBUTES","AUTHORIZATION","AVG","BEFORE","BEGIN","BEGIN_FRAME","BEGIN_PARTITION","BERNOULLI","BETWEEN","BIGINT","BINARY","BIT","BLOB","BOOLEAN","BOTH","BREADTH","BY","C","CALL","CALLED","CARDINALITY","CASCADE","CASCADED","CASE","CAST","CATALOG","CATALOG_NAME","CEIL","CEILING","CENTURY","CHAIN","CHAR","CHARACTER","CHARACTERISTICS","CHARACTERS","CHARACTER_LENGTH","CHARACTER_SET_CATALOG","CHARACTER_SET_NAME","CHARACTER_SET_SCHEMA","CHAR_LENGTH","CHECK","CLASSIFIER","CLASS_ORIGIN","CLOB","CLOSE","COALESCE","COBOL","COLLATE","COLLATION","COLLATION_CATALOG","COLLATION_NAME","COLLATION_SCHEMA","COLLECT","COLUMN","COLUMN_NAME","COMMAND_FUNCTION","COMMAND_FUNCTION_CODE","COMMIT","COMMITTED","CONDITION","CONDITION_NUMBER","CONNECT","CONNECTION","CONNECTION_NAME","CONSTRAINT","CONSTRAINTS","CONSTRAINT_CATALOG","CONSTRAINT_NAME","CONSTRAINT_SCHEMA","CONSTRUCTOR","CONTAINS","CONTINUE","CONVERT","CORR","CORRESPONDING","COUNT","COVAR_POP","COVAR_SAMP","CREATE","CROSS","CUBE","CUME_DIST","CURRENT","CURRENT_CATALOG","CURRENT_DATE","CURRENT_DEFAULT_TRANSFORM_GROUP","CURRENT_PATH","CURRENT_ROLE","CURRENT_ROW","CURRENT_SCHEMA","CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_TRANSFORM_GROUP_FOR_TYPE","CURRENT_USER","CURSOR","CURSOR_NAME","CYCLE","DATA","DATABASE","DATE","DATETIME_INTERVAL_CODE","DATETIME_INTERVAL_PRECISION","DAY","DEALLOCATE","DEC","DECADE","DECIMAL","DECLARE","DEFAULT","DEFAULTS","DEFERRABLE","DEFERRED","DEFINE","DEFINED","DEFINER","DEGREE","DELETE","DENSE_RANK","DEPTH","DEREF","DERIVED","DESC","DESCRIBE","DESCRIPTION","DESCRIPTOR","DETERMINISTIC","DIAGNOSTICS","DISALLOW","DISCONNECT","DISPATCH","DISTINCT","DOMAIN","DOUBLE","DOW","DOY","DROP","DYNAMIC","DYNAMIC_FUNCTION","DYNAMIC_FUNCTION_CODE","EACH","ELEMENT","ELSE","EMPTY","END","END-EXEC","END_FRAME","END_PARTITION","EPOCH","EQUALS","ESCAPE","EVERY","EXCEPT","EXCEPTION","EXCLUDE","EXCLUDING","EXEC","EXECUTE","EXISTS","EXP","EXPLAIN","EXTEND","EXTERNAL","EXTRACT","FALSE","FETCH","FILTER","FINAL","FIRST","FIRST_VALUE","FLOAT","FLOOR","FOLLOWING","FOR","FOREIGN","FORTRAN","FOUND","FRAC_SECOND","FRAME_ROW","FREE","FROM","FULL","FUNCTION","FUSION","G","GENERAL","GENERATED","GEOMETRY","GET","GLOBAL","GO","GOTO","GRANT","GRANTED","GROUP","GROUPING","GROUPS","HAVING","HIERARCHY","HOLD","HOUR","IDENTITY","IMMEDIATE","IMMEDIATELY","IMPLEMENTATION","IMPORT","IN","INCLUDING","INCREMENT","INDICATOR","INITIAL","INITIALLY","INNER","INOUT","INPUT","INSENSITIVE","INSERT","INSTANCE","INSTANTIABLE","INT","INTEGER","INTERSECT","INTERSECTION","INTERVAL","INTO","INVOKER","IS","ISOLATION","JAVA","JOIN","JSON","K","KEY","KEY_MEMBER","KEY_TYPE","LABEL","LAG","LANGUAGE","LARGE","LAST","LAST_VALUE","LATERAL","LEAD","LEADING","LEFT","LENGTH","LEVEL","LIBRARY","LIKE","LIKE_REGEX","LIMIT","LN","LOCAL","LOCALTIME","LOCALTIMESTAMP","LOCATOR","LOWER","M","MAP","MATCH","MATCHED","MATCHES","MATCH_NUMBER","MATCH_RECOGNIZE","MAX","MAXVALUE","MEASURES","MEMBER","MERGE","MESSAGE_LENGTH","MESSAGE_OCTET_LENGTH","MESSAGE_TEXT","METHOD","MICROSECOND","MILLENNIUM","MIN","MINUS","MINUTE","MINVALUE","MOD","MODIFIES","MODULE","MONTH","MORE","MULTISET","MUMPS","NAME","NAMES","NATIONAL","NATURAL","NCHAR","NCLOB","NESTING","NEW","NEXT","NO","NONE","NORMALIZE","NORMALIZED","NOT","NTH_VALUE","NTILE","NULL","NULLABLE","NULLIF","NULLS","NUMBER","NUMERIC","OBJECT","OCCURRENCES_REGEX","OCTETS","OCTET_LENGTH","OF","OFFSET","OLD","OMIT","ON","ONE","ONLY","OPEN","OPTION","OPTIONS","OR","ORDER","ORDERING","ORDINALITY","OTHERS","OUT","OUTER","OUTPUT","OVER","OVERLAPS","OVERLAY","OVERRIDING","PAD","PARAMETER","PARAMETER_MODE","PARAMETER_NAME","PARAMETER_ORDINAL_POSITION","PARAMETER_SPECIFIC_CATALOG","PARAMETER_SPECIFIC_NAME","PARAMETER_SPECIFIC_SCHEMA","PARTIAL","PARTITION","PASCAL","PASSTHROUGH","PAST","PATH","PATTERN","PER","PERCENT","PERCENTILE_CONT","PERCENTILE_DISC","PERCENT_RANK","PERIOD","PERMUTE","PLACING","PLAN","PLI","PORTION","POSITION","POSITION_REGEX","POWER","PRECEDES","PRECEDING","PRECISION","PREPARE","PRESERVE","PREV","PRIMARY","PRIOR","PRIVILEGES","PROCEDURE","PUBLIC","QUARTER","RANGE","RANK","READ","READS","REAL","RECURSIVE","REF","REFERENCES","REFERENCING","REGR_AVGX","REGR_AVGY","REGR_COUNT","REGR_INTERCEPT","REGR_R2","REGR_SLOPE","REGR_SXX","REGR_SXY","REGR_SYY","RELATIVE","RELEASE","REPEATABLE","REPLACE","RESET","RESTART","RESTRICT","RESULT","RETURN","RETURNED_CARDINALITY","RETURNED_LENGTH","RETURNED_OCTET_LENGTH","RETURNED_SQLSTATE","RETURNS","REVOKE","RIGHT","ROLE","ROLLBACK","ROLLUP","ROUTINE","ROUTINE_CATALOG","ROUTINE_NAME","ROUTINE_SCHEMA","ROW","ROWS","ROW_COUNT","ROW_NUMBER","RUNNING","SAVEPOINT","SCALE","SCHEMA","SCHEMA_NAME","SCOPE","SCOPE_CATALOGS","SCOPE_NAME","SCOPE_SCHEMA","SCROLL","SEARCH","SECOND","SECTION","SECURITY","SEEK","SELECT","SELF","SENSITIVE","SEQUENCE","SERIALIZABLE","SERVER","SERVER_NAME","SESSION","SESSION_USER","SET","SETS","SHOW","SIMILAR","SIMPLE","SIZE","SKIP","SMALLINT","SOME","SOURCE","SPACE","SPECIFIC","SPECIFICTYPE","SPECIFIC_NAME","SQL","SQLEXCEPTION","SQLSTATE","SQLWARNING","SQL_BIGINT","SQL_BINARY","SQL_BIT","SQL_BLOB","SQL_BOOLEAN","SQL_CHAR","SQL_CLOB","SQL_DATE","SQL_DECIMAL","SQL_DOUBLE","SQL_FLOAT","SQL_INTEGER","SQL_INTERVAL_DAY","SQL_INTERVAL_DAY_TO_HOUR","SQL_INTERVAL_DAY_TO_MINUTE","SQL_INTERVAL_DAY_TO_SECOND","SQL_INTERVAL_HOUR","SQL_INTERVAL_HOUR_TO_MINUTE","SQL_INTERVAL_HOUR_TO_SECOND","SQL_INTERVAL_MINUTE","SQL_INTERVAL_MINUTE_TO_SECOND","SQL_INTERVAL_MONTH","SQL_INTERVAL_SECOND","SQL_INTERVAL_YEAR","SQL_INTERVAL_YEAR_TO_MONTH","SQL_LONGVARBINARY","SQL_LONGVARCHAR","SQL_LONGVARNCHAR","SQL_NCHAR","SQL_NCLOB","SQL_NUMERIC","SQL_NVARCHAR","SQL_REAL","SQL_SMALLINT","SQL_TIME","SQL_TIMESTAMP","SQL_TINYINT","SQL_TSI_DAY","SQL_TSI_FRAC_SECOND","SQL_TSI_HOUR","SQL_TSI_MICROSECOND","SQL_TSI_MINUTE","SQL_TSI_MONTH","SQL_TSI_QUARTER","SQL_TSI_SECOND","SQL_TSI_WEEK","SQL_TSI_YEAR","SQL_VARBINARY","SQL_VARCHAR","SQRT","START","STATE","STATEMENT","STATIC","STDDEV_POP","STDDEV_SAMP","STREAM","STRUCTURE","STYLE","SUBCLASS_ORIGIN","SUBMULTISET","SUBSET","SUBSTITUTE","SUBSTRING","SUBSTRING_REGEX","SUCCEEDS","SUM","SYMMETRIC","SYSTEM","SYSTEM_TIME","SYSTEM_USER","TABLE","TABLESAMPLE","TABLE_NAME","TEMPORARY","THEN","TIES","TIME","TIMESTAMP","TIMESTAMPADD","TIMESTAMPDIFF","TIMEZONE_HOUR","TIMEZONE_MINUTE","TINYINT","TO","TOP_LEVEL_COUNT","TRAILING","TRANSACTION","TRANSACTIONS_ACTIVE","TRANSACTIONS_COMMITTED","TRANSACTIONS_ROLLED_BACK","TRANSFORM","TRANSFORMS","TRANSLATE","TRANSLATE_REGEX","TRANSLATION","TREAT","TRIGGER","TRIGGER_CATALOG","TRIGGER_NAME","TRIGGER_SCHEMA","TRIM","TRIM_ARRAY","TRUE","TRUNCATE","TYPE","UESCAPE","UNBOUNDED","UNCOMMITTED","UNDER","UNION","UNIQUE","UNKNOWN","UNNAMED","UNNEST","UPDATE","UPPER","UPSERT","USAGE","USER","USER_DEFINED_TYPE_CATALOG","USER_DEFINED_TYPE_CODE","USER_DEFINED_TYPE_NAME","USER_DEFINED_TYPE_SCHEMA","USING","VALUE","VALUES","VALUE_OF","VARBINARY","VARCHAR","VARYING","VAR_POP","VAR_SAMP","VERSION","VERSIONING","VIEW","WEEK","WHEN","WHENEVER","WHERE","WIDTH_BUCKET","WINDOW","WITH","WITHIN","WITHOUT","WORK","WRAPPER","WRITE","XML","YEAR","ZONE");
        List<SqlLexerToken> lexer_tokens = new ArrayList<>();

//        // Check for token injection
//        for(String token : sql_tokens){
//            Pattern pattern = Pattern.compile("(?<=\\s|^|[;])" + token.toLowerCase(Locale.ROOT) +"(?=\\s|$)");
//            Matcher matcher = pattern.matcher(sql_query.toLowerCase(Locale.ROOT));
//            while(matcher.find()){
//                lexer_tokens.add(new SqlLexerToken(matcher.start(),matcher.end(),token.toLowerCase(Locale.ROOT),0));
//            }
//        }

        try {
            // Parse SQL Query
            SqlParser parser = SqlParser.create(sql_query);
            SqlNode node = parser.parseStmt();
            // Join lines if separated by a comma and new line
            String node_string = node.toString().replaceAll(",\\r\\n",",");
            // Split tokens separated by new line
            String[] tokens = node_string.split("\\r?\\n");
            // Cursor initialized
            int cursor = 0;
            // Token position of the array
            int count = 0;
            // Traverse through tokens to remove extra backticks and spaces added by the SqlParser
            for(String token : tokens){
                char[] c_Arr = token.toLowerCase(Locale.ROOT).toCharArray();
                List<Character> final_token = new ArrayList<>();
                // Starting Position of token
                int startPos = cursor;
                // Traverse through the characters of parsed string and compare with corresponding characters of original string
                for(char c : c_Arr){
                    if(cursor >= sql_query.length()){
                        break;
                    }
                    if(c == sql_query.toLowerCase(Locale.ROOT).charAt(cursor)){
                        // If characters match, increment the cursor
                        final_token.add(c);
                        cursor++;
                    }
                }
                // If not the end of token array, increment the cursor for space between two consecutive tokens
                if (count < tokens.length - 1){
                    cursor++;
                }
                // End Position of token
                int endPos = cursor;
                StringBuilder sb = new StringBuilder();
                for (Character ch : final_token) {
                    sb.append(ch);
                }
                String token_string = sb.toString();
                // Increment token position of the array
                count++;
                // Create the corresponding LexerToken
                lexer_tokens.add(new SqlLexerToken(startPos,endPos,token_string,0));
            }
        } catch (Exception e) {
            // System.out.println("sql parsing exception : " + Arrays.toString(e.getStackTrace()));
            lexer_tokens.add(new SqlLexerToken(0,sql_query.length()-1,"SQL Parsing Error",0));
            return lexer_tokens;
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

    public static void main(String[] args) throws SqlParseException, IOException {
        // String sqlString = "insert into emp values(1,2)";
        String sqlString = "select * from emp,der,dwad,dwiad where id=2 group by sed";
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




