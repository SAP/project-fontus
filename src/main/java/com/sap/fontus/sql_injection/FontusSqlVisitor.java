package com.sap.fontus.sql_injection;

import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.util.SqlVisitor;

import java.util.Arrays;

public class FontusSqlVisitor implements SqlVisitor {
    @Override
    public Object visit(SqlLiteral literal) {
        SqlParserPos pos = literal.getParserPosition();
        System.out.println("SqlLiteral columnNum : " + pos.getColumnNum());
        System.out.println("SqlLiteral endColumnNum : " + pos.getEndColumnNum());
        return pos;
    }

    @Override
    public Object visit(SqlCall call) {
        SqlParserPos pos = call.getParserPosition();
        System.out.println("SqlCall getLineNum : " + pos.getLineNum());
        System.out.println("SqlCall getEndLineNum : " + pos.getEndLineNum());
        System.out.println("SqlCall columnNum : " + pos.getColumnNum());
        System.out.println("SqlCall endColumnNum : " + pos.getEndColumnNum());
        System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        return pos;
    }

    public Object visit(SqlNode node) {
        SqlParserPos pos = node.getParserPosition();
        System.out.println("SqlNode columnNum : " + pos.getColumnNum());
        System.out.println("SqlNode endColumnNum : " + pos.getEndColumnNum());
        return pos;
    }

    @Override
    public Object visit(SqlNodeList nodeList) {
        SqlParserPos pos = nodeList.getParserPosition();
        System.out.println("SqlNodeList columnNum : " + pos.getColumnNum());
        System.out.println("SqlNodeList endColumnNum : " + pos.getEndColumnNum());
        return pos;
    }

    @Override
    public Object visit(SqlIdentifier id) {
        SqlParserPos pos = id.getParserPosition();
        System.out.println("SqlIdentifier getSimple : " + id.getSimple());
        System.out.println("SqlIdentifier columnNum : " + pos.getColumnNum());
        System.out.println("SqlIdentifier endColumnNum : " + pos.getEndColumnNum());
        return pos;
    }

    @Override
    public Object visit(SqlDataTypeSpec type) {
        SqlParserPos pos = type.getParserPosition();
        System.out.println("SqlDataTypeSpec columnNum : " + pos.getColumnNum());
        System.out.println("SqlDataTypeSpec endColumnNum : " + pos.getEndColumnNum());
        return pos;
    }

    @Override
    public Object visit(SqlDynamicParam param) {
        SqlParserPos pos = param.getParserPosition();
        System.out.println("SqlDynamicParam columnNum : " + pos.getColumnNum());
        System.out.println("SqlDynamicParam endColumnNum : " + pos.getEndColumnNum());
        return pos;
    }

    @Override
    public Object visit(SqlIntervalQualifier intervalQualifier) {
        SqlParserPos pos = intervalQualifier.getParserPosition();
        System.out.println("SqlIntervalQualifier columnNum : " + pos.getColumnNum());
        System.out.println("SqlIntervalQualifier endColumnNum : " + pos.getEndColumnNum());
        return pos;
    }
}
