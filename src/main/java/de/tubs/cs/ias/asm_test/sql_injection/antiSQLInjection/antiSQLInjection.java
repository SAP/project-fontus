package de.tubs.cs.ias.asm_test.sql_injection.antiSQLInjection;


import gudusoft.gsqlparser.EDbVendor;
import org.json.JSONArray;
import org.json.JSONObject;

public class antiSQLInjection  {

    public static void main(String args[])
     {
         String sqltext = "SELECT * FROM products WHERE id = 10 or 1=1;--";
         TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
         if (anti.isInjected(sqltext)){
            System.out.println("SQL injected found:");
            for(int i=0;i<anti.getSqlInjections().size();i++){
                System.out.println("type: "+anti.getSqlInjections().get(i).getType()+", description: "+ anti.getSqlInjections().get(i).getDescription());
            }
         }else {
             System.out.println("Not injected");
         }

     }

     public static JSONArray getSqlInjectionInfo(String query_string){
         TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
         JSONArray detected_sql_injection_array = new JSONArray();
         if (anti.isInjected(query_string)) {
             for (int i = 0; i < anti.getSqlInjections().size(); i++) {
                 JSONObject detected_sql_injection_obj = new JSONObject();
                 detected_sql_injection_obj.put("type", anti.getSqlInjections().get(i).getType());
                 detected_sql_injection_obj.put("description", anti.getSqlInjections().get(i).getDescription());
                 detected_sql_injection_array.put(detected_sql_injection_obj);
             }
         }
         return detected_sql_injection_array;
     }

}