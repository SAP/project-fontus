package com.sap.fontus.sql;

import com.sap.fontus.sql.tainter.StatementTainter;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatementTainterTests {

    @Test
    void testNestedQuery() throws JSQLParserException {
        String query = "select 'a' as foo, (select count(id) from bla) as bar";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);

        StatementTainter tainter = new StatementTainter();
        //System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        assertEquals("SELECT 'a' AS foo, '0' AS `__taint__foo`, (SELECT count(id) FROM bla) AS bar, (SELECT '0' FROM bla LIMIT 1) AS `__taint__bar`;", taintedStatement.trim());
    }

    @Test
    void testNestedQueryWithWhere() throws JSQLParserException {
        String query = "select 'a' as foo, (select b from bla where id < 5) as bar";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);

        StatementTainter tainter = new StatementTainter();
        //System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        assertEquals("SELECT 'a' AS foo, '0' AS `__taint__foo`, (SELECT b FROM bla WHERE id < 5) AS bar, (SELECT `__taint__b` FROM bla WHERE id < 5) AS `__taint__bar`;", taintedStatement.trim());
    }

    @Test
    void h2iswtftest() throws Exception {
        String query =
                "SELECT T0.\"ID\" as \"ID\", T0.\"CREATEDBY\" as \"createdBy\", T0.\"MODIFIEDAT\" as \"modifiedAt\", T0.\"RATING\" as \"rating\", T0.\"TITLE\" as \"title\", T0.\"ISACTIVEENTITY\" as \"IsActiveEntity\", T0.\"HASACTIVEENTITY\" as \"HasActiveEntity\", T1.\"ID\" as \"book.ID\", T1.\"TITLE\" as \"book.title\", T2.\"ID\" as \"book.author.ID\", T2.\"NAME\" as \"book.author.name\", T2.\"ID\" as \"book.author.@audit:DS_ID\", T2.\"ID\" as \"book.author.@audit:ID\", T0.\"ISACTIVEENTITY\" as \"@IsActiveEntity\", T0.\"ID\" as \"@ID\" FROM (SELECT ACTIVE.*, true as IsActiveEntity, false as HasActiveEntity from \"REVIEWSERVICE_REVIEWS\" ACTIVE) T0 LEFT OUTER JOIN \"REVIEWSERVICE_BOOKS\" T1 ON T0.\"BOOK_ID\" = T1.\"ID\" LEFT OUTER JOIN \"REVIEWSERVICE_AUTHORS\" T2 ON T1.\"AUTHOR_ID\" = T2.\"ID\" LEFT OUTER JOIN \"REVIEWSERVICE_REVIEWS_DRAFTS\" T3 ON T3.\"ID\" = T0.\"ID\" and (T3.\"DRAFTADMINISTRATIVEDATA_DRAFTUUID\" IN (SELECT DraftUUID FROM DRAFT_DraftAdministrativeData where CreatedByUser is null or ? = CreatedByUser)) WHERE T0.\"ISACTIVEENTITY\" = FALSE and T0.\"ISACTIVEENTITY\" is not NULL or T3.\"ISACTIVEENTITY\" is NULL ORDER BY T0.\"MODIFIEDAT\" DESC NULLS LAST, T0.\"ID\" NULLS FIRST, T0.\"ISACTIVEENTITY\" NULLS FIRST LIMIT 30";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);
        StatementTainter tainter = new StatementTainter();
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();


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

        StatementTainter tainter = new StatementTainter();
        //System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        //System.out.println(taintedStatement);
        String result = "CREATE TABLE `o_ac_method` (`method_id` bigint (20) NOT NULL, `__taint__method_id` TEXT, `access_method` varchar (32) DEFAULT NULL, `__taint__access_method` TEXT, `version` mediumint (8) unsigned NOT NULL, `__taint__version` TEXT, `creationdate` datetime DEFAULT NULL, `__taint__creationdate` TEXT, `lastmodified` datetime DEFAULT NULL, `__taint__lastmodified` TEXT, `is_valid` bit (1) DEFAULT b'1', `__taint__is_valid` TEXT, `is_enabled` bit (1) DEFAULT b'1', `__taint__is_enabled` TEXT, `validfrom` datetime DEFAULT NULL, `__taint__validfrom` TEXT, `validto` datetime DEFAULT NULL, `__taint__validto` TEXT, PRIMARY KEY (`method_id`)) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;";
        assertEquals(result, taintedStatement.trim());

    }
}
