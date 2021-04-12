package com.sap.fontus.sql_injection;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqlParsingTest {

    @Test
    public void checkSelectAllQueryParsing(){
        SqlParser parser = SqlParser.create("select * from table");
        try {
            SqlNode node = parser.parseStmt();
            String node_string = node.toString().replaceAll(",\\r\\n",",");
            String[] lines = node_string.split("\\r\\n | \\r | \\n");
            assertFalse(lines.length != 2, "select * statement should have 2 lines");
        } catch (SqlParseException e) {
            System.out.println("Sql Parsing Error");
        }
    }

    @Test
    public void checkSelectSomeWhereQueryParsing(){
        SqlParser parser = SqlParser.create("select column1 from table where column2 = data");
        try {
            SqlNode node = parser.parseStmt();
            String node_string = node.toString().replaceAll(",\\r\\n",",");
            String[] lines = node_string.split("\\r\\n | \\r | \\n");
            assertFalse(lines.length != 3, "select with where statement should have 2 lines");
        } catch (SqlParseException e) {
            System.out.println("Sql Parsing Error");
        }
    }

    @Test
    public void checkInsertQueryParsing(){
        SqlParser parser = SqlParser.create("insert into table1 values(data1, data2)");
        try {
            SqlNode node = parser.parseStmt();
            String node_string = node.toString().replaceAll(",\\r\\n",",");
            String[] lines = node_string.split("\\r\\n | \\r | \\n");
            assertFalse(lines.length != 2, "insert statement should have 2 lines");
        } catch (SqlParseException e) {
            System.out.println("Sql Parsing Error");
        }
    }
}
