package com.sap.fontus.sql_injection.antiSQLInjection.test;


import com.sap.fontus.sql_injection.antiSQLInjection.TAntiSQLInjection;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.ESqlStatementType;
import junit.framework.TestCase;

public class antiSQLInjectionTest extends TestCase {

    public void testSyntaxError(){
        String sqltext = "select col1  from table1 where ";
        TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
        assertTrue(anti.isInjected(sqltext));
        assertTrue(anti.getSqlInjections().get(0).getType().toString().equalsIgnoreCase("syntax_error"));
    }

    public void testAlways_true_condition(){
        String sqltext = "select col1  from table1 where col1 > 1 or 1=1";
        TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
        assertTrue(anti.isInjected(sqltext));
        assertTrue(anti.getSqlInjections().get(0).getType().toString().equalsIgnoreCase("always_true_condition"));
    }

    public void testAlways_false_condition(){
        String sqltext = "select col1  from table1 where col1 > 1 and 1=2";
        TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
        assertTrue(anti.isInjected(sqltext));
        assertTrue(anti.getSqlInjections().get(0).getType().toString().equalsIgnoreCase("always_false_condition"));
    }

    public void testComment_at_the_end_of_statement(){
        String sqltext = "select col1  from table1 where col1 > 1; -- comment at the end of sql statement, maybe a sql injection";
        TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
        assertTrue(anti.isInjected(sqltext));
        assertTrue(anti.getSqlInjections().get(0).getType().toString().equalsIgnoreCase("comment_at_the_end_of_statement"));
    }

    public void testStacking_queries(){
        String sqltext = "select col1  from table1 where col1 > 1; drop table t1;";
        TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
        assertTrue(anti.isInjected(sqltext));
        assertTrue(anti.getSqlInjections().get(0).getType().toString().equalsIgnoreCase("stacking_queries"));
    }

    public void testUnion_set(){
        String sqltext = "select col1  from table1 where col1 > 1 union select col2 from table2";
        TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
        //anti.check_union_set(false);
        assertTrue(anti.isInjected(sqltext));
        assertTrue(anti.getSqlInjections().get(0).getType().toString().equalsIgnoreCase("union_set"));
    }


    public void testNot_in_allowed_statement(){
        String sqltext = "select col1  from table1 where col1 > 1; update table2 set col1=1";
        TAntiSQLInjection anti = new TAntiSQLInjection(EDbVendor.dbvoracle);
        assertTrue(anti.isInjected(sqltext));
        assertTrue(anti.getSqlInjections().get(0).getType().toString().equalsIgnoreCase("stacking_queries"));
        assertTrue(anti.getSqlInjections().get(1).getType().toString().equalsIgnoreCase("not_in_allowed_statement"));

        anti.enableStatement(ESqlStatementType.sstupdate);
        anti.check_stacking_queries(false);

        assertTrue(!anti.isInjected(sqltext));

    }

}
