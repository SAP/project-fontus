package com.sap.fontus.sanitizer;

import com.sap.fontus.taintaware.shared.IASTaintRange;

public class SqlTokenOverlap {

    private final SqlLexerToken token;
    private final IASTaintRange range;

    public SqlTokenOverlap(SqlLexerToken token, IASTaintRange range) {
        this.token = token;
        this.range = range;
    }

    public SqlLexerToken getToken() {
        return this.token;
    }

    public IASTaintRange getRange() {
        return this.range;
    }

    @Override
    public String toString() {
        return "SqlTokenOverlap{" +
                "token=" + this.token +
                ", range=" + this.range +
                '}';
    }
}