package com.sap.fontus.sql_injection;

public class SqlLexerToken {
    public int begin;
    public int end;
    public String token;
    public int token_type;

    public SqlLexerToken(int begin,int end,String token,int token_type){
        this.begin = begin;
        this.end = end;
        this.token = token;
        this.token_type = token_type;
    }
}
