package com.sap.fontus.sanitizer;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.*;

import java.util.ArrayList;
import java.util.List;

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

    public static List<SqlLexerToken> getLexerTokens(String sqlQuery) {
        // System.out.println("SQL Query : " + sqlQuery);
        List<SqlLexerToken> lexerTokens = new ArrayList<>();
        DbType dbType = DbType.postgresql;

        try {
            // Parse SQL Query
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sqlQuery, dbType);
            List<SQLStatement> statementList = parser.parseStatementList();
            if(statementList.size() > 1){
                lexerTokens.add(new SqlLexerToken(0,sqlQuery.length()-1,Token.EOF,1));
                return lexerTokens;
            }
            Lexer lexer = SQLParserUtils.createLexer(sqlQuery, dbType);
            int startPos = lexer.pos();
            lexer.nextToken();
            while(lexer.token() != Token.EOF){
                // Create the corresponding LexerToken
                lexerTokens.add(new SqlLexerToken(startPos,lexer.pos(),lexer.token(),0));
                // System.out.println("tokenType : " + lexer.token() + ", startPos : " + startPos + ", endPos : " + lexer.pos());
                startPos = lexer.pos();
                lexer.nextToken();
            }
        } catch (ParserException e) {
            System.out.println("SQL Parsing Exception");
            lexerTokens.add(new SqlLexerToken(0,sqlQuery.length()-1,Token.ERROR,1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return lexerTokens;
    }
}
