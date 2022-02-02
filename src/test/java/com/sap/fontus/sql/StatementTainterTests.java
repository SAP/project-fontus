package com.sap.fontus.sql;

import com.sap.fontus.sql.tainter.StatementTainter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatementTainterTests {

    @Test
    void testNestedQuery() throws JSQLParserException {
        String query = "select 'a' as foo, (select count(id) from bla) as bar";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);

        StatementTainter tainter = new StatementTainter(new ArrayList<>());
        System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        assertEquals("SELECT 'a' AS foo, '0' AS `__taint__foo`, (SELECT count(id) FROM bla), (SELECT '0' from bla AS bar;", taintedStatement);

    }

    @Test
    void testBitDefault() throws Exception {
        String query = "CREATE TABLE `o_ac_method` (" +
                "  `method_id` bigint(20) NOT NULL," +
                "  `access_method` varchar(32) DEFAULT NULL," +
                "  `version` mediumint(8) unsigned NOT NULL," +
                "  `creationdate` datetime DEFAULT NULL," +
                "  `lastmodified` datetime DEFAULT NULL," +
                "  `is_valid` bit(1) DEFAULT b'1'," +
                "  `is_enabled` bit(1) DEFAULT b'1'," +
                "  `validfrom` datetime DEFAULT NULL," +
                "  `validto` datetime DEFAULT NULL," +
                "  PRIMARY KEY (`method_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);

        StatementTainter tainter = new StatementTainter(new ArrayList<>());
        System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        System.out.println(taintedStatement);
        String result = "CREATE TABLE `o_ac_method` (`method_id` bigint (20) NOT NULL, `__taint__method_id` TEXT, `access_method` varchar (32) DEFAULT NULL, `__taint__access_method` TEXT, `version` mediumint (8) unsigned NOT NULL, `__taint__version` TEXT, `creationdate` datetime DEFAULT NULL, `__taint__creationdate` TEXT, `lastmodified` datetime DEFAULT NULL, `__taint__lastmodified` TEXT, `is_valid` bit (1) DEFAULT b'1', `__taint__is_valid` TEXT, `is_enabled` bit (1) DEFAULT b'1', `__taint__is_enabled` TEXT, `validfrom` datetime DEFAULT NULL, `__taint__validfrom` TEXT, `validto` datetime DEFAULT NULL, `__taint__validto` TEXT, PRIMARY KEY (`method_id`)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;";
        assertEquals(result, taintedStatement);

    }
}
