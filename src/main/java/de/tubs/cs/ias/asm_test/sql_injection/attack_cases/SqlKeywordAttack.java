package de.tubs.cs.ias.asm_test.sql_injection.attack_cases;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SqlKeywordAttack {
    public static JSONObject checkSqlKeywordAttack(String tainted_string){
        String[] sql_primary_keywords = {"ALTER TABLE","ALTER COLUMN","INSERT INTO","SELECT","UPDATE"};
        String[] sql_secondary_keywords = {"ADD","ADD CONSTRAINT","ADD CHECK","DROP CHECK","DROP CONSTRAINT","DROP COLUMN",
                "DROP INDEX","DROP","ALTER COLUMN","MODIFY COLUMN","FROM","VALUES","SET"};
        String[] sql_single_keywords = {"CREATE TABLE","CREATE UNIQUE INDEX","CREATE INDEX","CREATE PROCEDURE",
                "CREATE DATABASE","CREATE OR REPLACE VIEW","CREATE VIEW","DELETE FROM","DROP TABLE","DROP VIEW","DROP DATABASE",
                "DROP INDEX","EXEC","TRUNCATE TABLE"};

        JSONObject sql_keyword_attack_obj = new JSONObject();

        sql_keyword_attack_obj.put("attack_type","SqlKeywordAttack");

        for(String s : sql_primary_keywords){
            if(tainted_string.toUpperCase().contains(s)){
//                List<Integer> foundIndexes = new ArrayList<>();
//                for (int i = -1; (i = tainted_string.indexOf(s, i + 1)) != -1; i++) {
//                    foundIndexes.add(i);
//                }
            }
        }

        return sql_keyword_attack_obj;
    }
}
