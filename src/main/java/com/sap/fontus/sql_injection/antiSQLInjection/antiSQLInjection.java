package com.sap.fontus.sql_injection.antiSQLInjection;


import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.ESqlStatementType;
import org.json.JSONArray;
import org.json.JSONObject;

public class antiSQLInjection  {

    public static void main(String args[])
     {
         String sqltext = "insert into table1 values (1,2); select * from x;";
         TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvpostgresql);
         anti.enableStatement(ESqlStatementType.sstinsert);
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
         TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvpostgresql);
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