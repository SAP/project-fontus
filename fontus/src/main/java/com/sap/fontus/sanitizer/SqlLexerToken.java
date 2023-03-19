package com.sap.fontus.sanitizer;

import com.alibaba.druid.sql.parser.Token;

public class SqlLexerToken {
    public final int begin;
    public final int end;
    public final Token token;
    public final int tokenType;

    public SqlLexerToken(int begin, int end, Token token, int tokenType){
        this.begin = begin;
        this.end = end;
        this.token = token;
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "SqlLexerToken{" +
                "begin=" + this.begin +
                ", end=" + this.end +
                ", token=" + this.token +
                ", token_type=" + this.tokenType +
                '}';
    }

    public Token getToken() {
        return this.token;
    }
}
