package com.sap.fontus.sql_injection;

public class SqlLexerToken {
    public int begin;
    public int end;
    public String token;
    public int token_type;
    public boolean has_comment;

    public SqlLexerToken(int begin,int end,String token,int token_type,boolean has_comment){
        this.begin = begin;
        this.end = end;
        this.token = token;
        this.token_type = token_type;
        this.has_comment = has_comment;
    }
}
