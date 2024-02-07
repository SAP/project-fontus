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
    void testOpenOlatFoobar() throws Exception {

        String query =
        "insert into o_stat_daily (businesspath, resid, day, value)\n" +
                "                                                select\n" +
                "                                                        delta.businesspath, delta.resid, delta.day, delta.cnt\n" +
                "                                                from (select\n" +
                "                                                                        businesspath,\n" +
                "                                                                        substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,\n" +
                "                                                                        date(creationdate) day,\n" +
                "                                                                        count(*) cnt\n" +
                "                                                        from o_stat_temptable\n" +
                "                                                        group by businesspath,day) delta\n" +
                "                                                        left join o_stat_daily old on (delta.businesspath=old.businesspath and delta.day=old.day)\n" +
                "                                                where old.businesspath is null;\n";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);

        StatementTainter tainter = new StatementTainter();
        //System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        assertEquals("INSERT INTO o_stat_daily (businesspath, `__taint__businesspath`, resid, `__taint__resid`, day, `__taint__day`, value, `__taint__value`) SELECT delta.businesspath, delta.`__taint__businesspath`, delta.resid, delta.`__taint__resid`, delta.day, delta.`__taint__day`, delta.cnt, delta.`__taint__cnt` FROM (SELECT businesspath, `__taint__businesspath`, substr(businesspath, locate(':', businesspath) + 1, locate(']', businesspath) - locate(':', businesspath) - 1) resid, '0' AS `__taint__resid`, date(creationdate) day, '0', count(*) cnt, '0' AS `__taint__cnt` FROM o_stat_temptable GROUP BY businesspath, `__taint__businesspath`, day, `__taint__day`) delta LEFT JOIN o_stat_daily old ON (delta.businesspath = old.businesspath AND delta.day = old.day) WHERE old.businesspath IS NULL;", taintedStatement.trim());

    }

    @Test
    void testInsert() throws JSQLParserException {
        String query = "Insert into foo values (2, 1), (3, 2);";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);

        StatementTainter tainter = new StatementTainter();
        //System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        assertEquals("INSERT INTO foo VALUES (2, '0', 1, '0'), (3, '0', 2, '0');", taintedStatement.trim());
    }

    @Test
    void testInsert2() throws JSQLParserException {
        String query = "INSERT INTO customers (name, vorname) VALUES ('Max', 'Mustermann'); insert into users VALUES ('peter') returning id;";
        Statements stmts = CCJSqlParserUtil.parseStatements(query);

        StatementTainter tainter = new StatementTainter();
        //System.out.println("Tainting: " + query);
        stmts.accept(tainter);
        String taintedStatement = stmts.toString();
        assertEquals("INSERT INTO customers (name, `__taint__name`, vorname, `__taint__vorname`) VALUES ('Max', '0', 'Mustermann', '0');\n" +
                "INSERT INTO users VALUES ('peter', '0') RETURNING id, `__taint__id`;", taintedStatement.trim());
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
