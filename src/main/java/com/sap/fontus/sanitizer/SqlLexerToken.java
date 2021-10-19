package com.sap.fontus.sanitizer;

import com.alibaba.druid.sql.parser.Token;

public class SqlLexerToken {
    public int begin;
    public int end;
    public Token token;
    public int token_type;

    public SqlLexerToken(int begin, int end, Token token, int token_type){
        this.begin = begin;
        this.end = end;
        this.token = token;
        this.token_type = token_type;
    }

    @Override
    public String toString() {
        return "SqlLexerToken{" +
                "begin=" + begin +
                ", end=" + end +
                ", token=" + token +
                ", token_type=" + token_type +
                '}';
    }

    public Token getToken() {
        return token;
    }
}
