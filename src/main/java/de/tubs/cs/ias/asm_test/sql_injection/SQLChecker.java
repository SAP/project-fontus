package de.tubs.cs.ias.asm_test.sql_injection;

import de.tubs.cs.ias.asm_test.sql_injection.antiSQLInjection.antiSQLInjection;
import de.tubs.cs.ias.asm_test.sql_injection.attack_cases.CommentLineAttack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SQLChecker {

    public static void checkTaintedString(String json_string) throws IOException {
        JSONObject json_obj = new JSONObject(json_string);
        String sql_string = json_obj.getString("payload");
        JSONArray input_ranges = json_obj.getJSONArray("ranges");
        for (int i = 0; i < input_ranges.length(); i++){
            int start_index = (int) input_ranges.getJSONObject(i).get("start");
            int end_index = (int) input_ranges.getJSONObject(i).get("end");
            String user_input = sql_string.substring(start_index,end_index);
            System.out.println(user_input);
            //checkAttack(user_input);
        }
    }

    public static void printCheck(String tainted_string) throws IOException {
        JSONObject json_obj = new JSONObject(tainted_string);
        String sql_string = json_obj.getString("payload");
        String json_string = antiSQLInjection.getSqlInjectionInfo(sql_string).toString();
        System.out.println(json_string);
    }

    private static void checkAttack(String tainted_string){
        JSONArray attack_results =new JSONArray();
        attack_results.put(CommentLineAttack.checkCommentLineAttack(tainted_string));
    }

    public static void main(String[] args) throws IOException {
        String tainted_json_string = "SELECT * FROM products WHERE id = 10 or 1=1;--";

        SQLChecker.printCheck(tainted_json_string);
    }
}
