package com.sap.fontus.sql_injection;

import com.sap.fontus.sql_injection.antiSQLInjection.antiSQLInjection;
import com.sap.fontus.utils.NetworkRequestObject;
import com.sap.fontus.utils.NetworkResponseObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SQLChecker {
    public static void checkTaintedString(String tainted_string) throws RuntimeException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InterruptedException {
        JSONObject json_obj = new JSONObject(tainted_string);
        String sql_string = json_obj.getString("payload");
        JSONArray json_array = antiSQLInjection.getSqlInjectionInfo(sql_string);
        System.out.println(json_array.toString());
        NetworkResponseObject.setResponseMessage(new NetworkRequestObject(),!json_array.isEmpty());
    }
}
