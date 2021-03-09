package de.tubs.cs.ias.asm_test.sql_injection;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tubs.cs.ias.asm_test.sql_injection.antiSQLInjection.antiSQLInjection;
import de.tubs.cs.ias.asm_test.utils.NetworkRequestObject;
import de.tubs.cs.ias.asm_test.utils.NetworkResponseObject;
import org.apache.calcite.sql.parser.SqlParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SQLChecker {

    public static void checkTaintedString(String tainted_string) throws RuntimeException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InterruptedException {
        JSONObject json_obj = new JSONObject(tainted_string);
        String sql_string = json_obj.getString("payload");
        JSONArray json_array = antiSQLInjection.getSqlInjectionInfo(sql_string);
        System.out.println(json_array.toString());
        NetworkRequestObject req_obj = new NetworkRequestObject();
        NetworkResponseObject.setResponseMessage(req_obj,!json_array.isEmpty());
    }

}
