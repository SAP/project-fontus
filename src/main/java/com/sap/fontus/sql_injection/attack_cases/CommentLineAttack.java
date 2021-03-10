package com.sap.fontus.sql_injection.attack_cases;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentLineAttack {
    public static JSONObject checkCommentLineAttack(String tainted_string){
        JSONObject comment_attack_obj = new JSONObject();

        comment_attack_obj.put("attack_type","CommentLineAttack");

        String dash_comment_regex = "[-]{2,}";
        Pattern dash_comment_pattern = Pattern.compile(dash_comment_regex);
        Matcher dash_comment_matcher = dash_comment_pattern.matcher(tainted_string);
        int dash_comment_count = dash_comment_matcher.groupCount();

        String hash_comment_regex = "[#]+";
        Pattern hash_comment_pattern = Pattern.compile(hash_comment_regex);
        Matcher hash_comment_matcher = hash_comment_pattern.matcher(tainted_string);
        int hash_comment_count = hash_comment_matcher.groupCount();

        String multiline_comment_regex = "(/*)*";
        Pattern multiline_comment_pattern = Pattern.compile(multiline_comment_regex);
        Matcher multiline_comment_matcher = multiline_comment_pattern.matcher(tainted_string);
        int multiline_comment_count = multiline_comment_matcher.groupCount();

        int comment_attacks_count = dash_comment_count + hash_comment_count + multiline_comment_count;
        System.out.println("dash = " + dash_comment_count);
        System.out.println("hash = " + hash_comment_count);
        System.out.println("multi = " + multiline_comment_count);
        comment_attack_obj.put("attack_count",comment_attacks_count);

        comment_attack_obj.put("attack_detected", comment_attacks_count > 0);
        return comment_attack_obj;
    }

    public static void main(String[] args) {
        System.out.println(CommentLineAttack.checkCommentLineAttack("/*dwadaw*//*dwaaw").toString());

    }
}
