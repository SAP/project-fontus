package com.sap.fontus.sql_injection;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.Token;
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqlParsingTest {

    @Test
    public void checkMultipleQueryParsing(){
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser("select * from table1; drop table table1", DbType.mysql);
        List<SQLStatement> statementList = parser.parseStatementList();
        assertFalse(statementList.size() != 2, "sql string should parse as 2 statements");
    }

    @Test
    public void checkTokenParsing(){
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser("select column1 from table where column2='data'"
                , DbType.mysql);
        List<SQLStatement> statementList = parser.parseStatementList();
        assertFalse(statementList.size() != 1, "sql string is a single statement");
        Lexer lexer = SQLParserUtils.createLexer("select column1 from table where column2='data'", DbType.mysql);
        lexer.nextToken();
        int count = 0;
        while(lexer.token() != Token.EOF){
            count++;
            lexer.nextToken();
        }
        assertFalse(count != 8, "incorrect no. of tokens while parsing");
    }
}
