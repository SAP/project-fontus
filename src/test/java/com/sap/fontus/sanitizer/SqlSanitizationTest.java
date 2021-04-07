package com.sap.fontus.sanitizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * JUnit test for sanitization of sql statements.
 */
@RunWith(Parameterized.class)
public class SqlSanitizationTest {

    private static String JDBC_DRIVER = org.h2.Driver.class.getName();
    private static String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static String USER = "user";
    private static String PASSWORD = "";

    @BeforeClass
    public static void createDatabaseSchema() throws Exception {
        RunScript.execute(JDBC_URL, USER, PASSWORD, "src//test//java//com//sap//resources//Schema.sql", null, false);
    }

    @Before
    public void importDb() throws Exception {
        IDataSet dataSet = new FlatXmlDataSetBuilder()
                .build(new File("src//test//java//com//sap//resources//Students.xml"));
        IDatabaseTester dbTester = new JdbcDatabaseTester(JDBC_DRIVER, JDBC_URL, USER, PASSWORD);
        dbTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        dbTester.setDataSet(dataSet);
        dbTester.onSetup();
    }

    @Parameters(name = "{index}: {1}")
    public static Collection<Object[]> data() throws SQLException {
        // Add this for testing ms access db
        // Connection accessConnection = DriverManager.getConnection(sourcepath);
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(JDBC_URL);
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);
        Connection mySqlConnection = dataSource.getConnection();

        return Arrays.asList(new Object[][] { { mySqlConnection, "MySql" } });
    }

    private Connection con;

    public SqlSanitizationTest(Connection con) {
        this.con = con;
    }

    private Connection getConnection() {
        return this.con;
    }

    /**
     * One complete attribute value is tainted. Wouldn't have changed syntax of
     * query.
     */
    @Test
    public void testAttributeValueTainted_1() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        // London is tainted, i.e. the '' are NOT included in the taint
        ranges.add(new IASTaintRange(taintedString.length() - 7, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed
        assertNotNull(rs);
        // resultset not empty because there is a match for city=london
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * One complete attribute value is tainted. Interpreted as it is and not as part
     * of syntax and therfore converted into the string London.
     */
    @Test
    public void testAttributeValueTainted_2() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE City=4c6f6e646f6e"; // hex for London
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 12, taintedString.length(), new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed
        assertNotNull(rs);
        // 0x4c6f6e646f6e is not interpreted as London because we use prp statements
        // no match found and result is empty
        assertFalse(rs.next());
    }

    /**
     * One complete attribute value is tainted. Would have changed syntax of query
     * because hex would have been interpreted as part of command.
     */
    @Test
    public void testAttributeValueTainted_3() throws SQLException {
        Connection con = this.getConnection();
        String badInput = "' OR 1=1--";
        String taintedString = "SELECT ID, City FROM Students WHERE City='" + badInput + "'";
        List<IASTaintRange> ranges = new ArrayList<>();
        // >>' OR 1=1--<< is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 11, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed
        assertNotNull(rs);
        // ' OR 1=1-- is not interpreted as code but as value due to the prepared
        // statements -> no match found and result is empty
        assertFalse(rs.next());
    }

    /**
     * One complete attribute value is tainted. Interpreted as integer.
     */
    @Test
    public void testAttributeValueTainted_4() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 1, taintedString.length(), new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);
        // sanitization was performed because attribute was tainted
        // number not being supported as datatype for ID
        assertNotNull(rs);
        // resultset not empty because there is a match for ID=1
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * One complete attribute value is tainted. Would have changed syntax of query
     * because hex would have been interpreted as part of command, not as integer.
     */
    @Test
    public void testAttributeValueTainted_5() throws SQLException {
        Connection con = this.getConnection();
        String badInput = "31204f5220313d312d2d"; // is hex representation of 1 OR 1=1--
        String taintedString = "SELECT ID, City FROM Students WHERE ID=" + badInput;
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 20, taintedString.length(), new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);
        // check if sanitization was aborted because exception was thrown due to hex
        // number not being supported as datatype for ID
        assertNull(rs);
    }

    /**
     * One complete attribute value is tainted, other attributes are not tainted.
     */
    @Test
    public void testAttributeValueTainted_6() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE 1=1 AND ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 19, taintedString.length() - 18, new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);
        // sanitization was performed because attribute was tainted
        // number not being supported as datatype for ID
        assertNotNull(rs);
        // resultset not empty because there is a match for ID=1
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Not all attribute values are tainted
     */
    @Test
    public void testAttributeValueTainted_7() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        // London is tainted, i.e. the '' are NOT included in the taint
        ranges.add(new IASTaintRange(taintedString.length() - 7, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange IS NOT empty
        // sanitization should be performed
        assertNotNull(rs);
        // resultset not empty because there is a match for city=london
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Multiple complete attribute values are tainted. Resultset is not empty.
     */
    @Test
    public void testAttributeValueTainted_8() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE City='London' AND ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        // London is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 16, taintedString.length() - 10, new IASTaintSource("dummy", 1234)));
        // 1 is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 1, taintedString.length(), new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // since taintRange IS NOT empty sanitization should be performed
        // check if sanitization was performed, i.e. no exception has been thrown
        assertNotNull(rs);
        // resultset not empty because there is a match for city=london
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Multiple complete attribute values are tainted. Resultset is not empty.
     */
    @Test
    public void testAttributeValueTainted_9() throws SQLException {
        Connection con = this.getConnection();
        String badInput = "' OR 1=1--";
        String taintedString = "SELECT ID, City FROM Students WHERE City='" + badInput + "' AND ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        // badInput is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 20, taintedString.length() - 10, new IASTaintSource("dummy", 1234)));
        // 1 is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 1, taintedString.length(), new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // since taintRange IS NOT empty sanitization should be performed
        // check if sanitization was performed, i.e. no exception has been thrown
        assertNotNull(rs);
        // resultset is empty because there is no match for city=' OR 1=1--
        // if it wouldn't have been interpreted as value but rather as sql query, then
        // the resultset would be the whole DB and rs.next()=true.
        assertFalse(rs.next());
    }

    /**
     * Tainted attribute value
     */
    @Test
    public void testAttributeValueTainted_10() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT TOP 1 ID FROM Students";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(11, 12, new IASTaintSource("dummy", 1234))); // 1 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // since taintRange IS NOT empty sanitization should be performed
        // check if sanitization was performed, i.e. no exception has been thrown
        assertNotNull(rs);
        // resultset is NOT empty
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertFalse(rs.next());
    }

    /**
     * Tainted attribute value
     */
    @Test
    public void testAttributeValueTainted_11() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID BETWEEN 1 AND 2";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 7, taintedString.length() - 6, new IASTaintSource("dummy", 1234))); // 1 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // since taintRange IS NOT empty sanitization should be performed
        // check if sanitization was performed, i.e. no exception has been thrown
        assertNotNull(rs);
        // resultset is NOT empty
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertTrue(rs.next());
        assertEquals("2", rs.getString("ID"));
        assertFalse(rs.next());
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_12() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 11, taintedString.length() - 9, new IASTaintSource("dummy", 1234))); // 20 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_13() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE City LIKE 'London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 7, taintedString.length() - 1, new IASTaintSource("dummy", 1234))); // London is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // sanitization was performed because attribute was tainted
        assertNotNull(rs);
        // resultset not empty because there is a match for London
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_14() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID IN(1,2)";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 2, taintedString.length() - 1, new IASTaintSource("dummy", 1234))); // 2 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // sanitization was performed because attribute was tainted
        assertNotNull(rs);
        // resultset not empty because there are ID's in [1,2]
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertTrue(rs.next());
        assertEquals("2", rs.getString("ID"));
        assertFalse(rs.next());
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_15() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE City='\' OR 1=1\''";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 10, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));// \' OR 1=1\' is
                                                                                                 // tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertFalse(rs.next());
    }

    /**
     * Tainted static value
     */
    @Test
    public void testAttributeValueTainted_16() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE TRUE=TRUE";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 4, taintedString.length(), new IASTaintSource("dummy", 1234)));// second TRUE is
                                                                                                // tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("Rome", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Tainted value with apostrophe in text. Thus it is a user input, the query
     * itself should have worked before applying changes due to the
     * sanitizeAndExecuteQuery method.
     */
    @Test
    public void testAttributeValueTainted_17() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE City='St. John\'s' OR City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        // St. John's is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 28, taintedString.length() - 18, new IASTaintSource("dummy", 1234)));
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Tainted value. But apostrophe in the another attribute text since it is not a
     * user input, the query itself should have worked before applying changes due
     * to the sanitizeAndExecuteQuery method. If it didn't work before, it won't
     * now. Handling this kind of problem is the responsibility of the dbs owner or
     * creator of the sql query.
     */
    @Test
    public void testAttributeValueTainted_18() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE City='St. John''s' OR City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 7, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));// London is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);
        // this query will cause problems due to the apostrophe in the city name.
        // this is to be expected
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Tainted value. But apostrophe in the another attribute text since it is not a
     * user input, the query itself should have worked before applying changes due
     * to the sanitizeAndExecuteQuery method. If it didn't work before, it won't
     * now. Handling this kind of problem is the responsibility of the dbs owner or
     * creator of the sql query.
     */
    @Test
    public void testAttributeValueTainted_19() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE City='London' OR City='St. John''s'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 29, taintedString.length() - 23, new IASTaintSource("dummy", 1234)));// London is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);
        // this query will cause problems due to the apostrophe in the city name.
        // this is to be expected
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_1() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(0, 6, new IASTaintSource("dummy", 1234)));// SELECT is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql command is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_2() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(16, 20, new IASTaintSource("dummy", 1234)));// FROM is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_3() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(30, 35, new IASTaintSource("dummy", 1234)));// WHERE is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_4() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students GROUP BY Students.City";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(26, 34, new IASTaintSource("dummy", 1234)));// GROUP BY is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_5() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students GROUP BY Students.City, Students.ID HAVING ID<2";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(66, 72, new IASTaintSource("dummy", 1234)));// HAVING is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_6() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students ORDER BY City";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(30, 38, new IASTaintSource("dummy", 1234)));// ORDER BY is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_7() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT s.ID, s.City FROM Students s INNER JOIN Students t ON s.City=t.City WHERE t.City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(36, 46, new IASTaintSource("dummy", 1234)));// INNER JOIN is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_8() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(41, 44, new IASTaintSource("dummy", 1234)));// AND is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_9() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(7, 10, new IASTaintSource("dummy", 1234)));// SUM is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_10() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(15, 17, new IASTaintSource("dummy", 1234)));// AS is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_11() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 3, taintedString.length(), new IASTaintSource("dummy", 1234)));// ASC is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_12() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT TOP 1 ID FROM Students";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(7, 10, new IASTaintSource("dummy", 1234)));// TOP is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_13() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID BETWEEN 1 AND 2";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 15, taintedString.length() - 8, new IASTaintSource("dummy", 1234))); // BETWEEN is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_14() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(0, 6, new IASTaintSource("dummy", 1234))); // CREATE is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as CREATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_15() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 8, taintedString.length() - 1, new IASTaintSource("dummy", 1234))); // INTEGER is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as CREATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_16() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 8, taintedString.length() - 5, new IASTaintSource("dummy", 1234))); // NOT is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_17() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 19, taintedString.length() - 12, new IASTaintSource("dummy", 1234))); // DEFAULT is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_18() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(21, 24, new IASTaintSource("dummy", 1234))); // ADD is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_19() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(0, 5, new IASTaintSource("dummy", 1234))); // ALTER is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_20() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "DROP TABLE Profs";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(0, 4, new IASTaintSource("dummy", 1234))); // DROP is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as DROP,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_21() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "TRUNCATE TABLE Profs";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(0, 8, new IASTaintSource("dummy", 1234))); // TRUNCATE is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as TRUNCATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_22() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID IS NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 11, taintedString.length(), new IASTaintSource("dummy", 1234))); // IS NOT NULL is
                                                                                                  // tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // sql keyword is tainted -> sanatization is aborted -> query NOT executed
        // -> return null
        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_23() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID IS NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 7, taintedString.length(), new IASTaintSource("dummy", 1234))); // IS NULL is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_24() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE City LIKE 'London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 13, taintedString.length() - 9, new IASTaintSource("dummy", 1234))); // LIKE is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_25() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID IN(1,2)";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 7, taintedString.length() - 5, new IASTaintSource("dummy", 1234))); // IN is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNull(rs);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_1() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(21, 29, new IASTaintSource("dummy", 1234)));// Students is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a table name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertNull(rs);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_2() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(13, 18, new IASTaintSource("dummy", 1234))); // Profs is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_3() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(12, 20, new IASTaintSource("dummy", 1234))); // Students is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_4() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "DROP TABLE Profs";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(11, 16, new IASTaintSource("dummy", 1234))); // Profs is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as DROP,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_5() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "TRUNCATE TABLE Profs";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(15, 20, new IASTaintSource("dummy", 1234))); // Profs is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as TRUNCATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL attribute/column name is tainted. Used existing column name. Query should
     * NOT be executed.
     */
    @Test
    public void testColumnNameTainted_1() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(7, 9, new IASTaintSource("dummy", 1234))); // first ID is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed. It was due to taint.
        // "ID" is interpreted as string and not as column. Therefore first result
        // column only contains the string "ID" even though there is a column with the
        // name ID
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("ID", rs.getString(1));
        assertEquals("London", rs.getString(2));
        assertFalse(rs.next());
    }

    /**
     * SQL attribute/column name is tainted. Used NOT existing column name. Query
     * should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_2() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT IDS, City FROM Students";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(7, 10, new IASTaintSource("dummy", 1234))); // first IDS is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed. It was due to taint.
        // "IDS" is interpreted as string and not as column. Therefore first result
        // column only contains the string "IDS" in each row
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("IDS", rs.getString(1));
        assertEquals("London", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("IDS", rs.getString(1));
        assertEquals("Rome", rs.getString(2));
        assertTrue(rs.next());
        assertEquals("IDS", rs.getString(1));
        assertEquals("St. John's", rs.getString(2));
        assertFalse(rs.next());
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_3() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(36, 38, new IASTaintSource("dummy", 1234))); // second ID is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertNull(rs);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_4() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 GROUP BY City";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(50, 54, new IASTaintSource("dummy", 1234)));// second City is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since only a sql column name for
        // grouping the result was tainted, the string should be sanitized
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_5() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(11, 13, new IASTaintSource("dummy", 1234)));// first ID is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertNull(rs);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_6() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(18, 24, new IASTaintSource("dummy", 1234)));// Sum_ID is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertNull(rs);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_7() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 8, taintedString.length() - 4, new IASTaintSource("dummy", 1234)));// third City is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertNull(rs);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_8() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 12, taintedString.length() - 9, new IASTaintSource("dummy", 1234))); // Age is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_9() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(25, 28, new IASTaintSource("dummy", 1234))); // Age is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertNull(rs);
    }

    /**
     * Part of SQL syntax and attribute value is tainted. Query should NOT be
     * executed.
     */
    @Test
    public void testMixedTainted_1() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 4, taintedString.length(), new IASTaintSource("dummy", 1234)));// ID=1 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql syntax and attribute values
        // are tainted, the sanatization should be aborted and the query NOT executed.
        // -> return null
        assertNull(rs);
    }

    /**
     * Part of SQL syntax and attribute value is tainted. Query should NOT be
     * executed.
     */
    @Test
    public void testMixedTainted_2() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(7, 8, new IASTaintSource("dummy", 1234)));// first ID is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 4, taintedString.length(), new IASTaintSource("dummy", 1234)));// ID=1 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql syntax and attribute values
        // are tainted, the sanatization should be aborted and the query NOT executed.
        // -> return null
        assertNull(rs);
    }

    /**
     * Part of SQL syntax and attribute value is tainted. Query should NOT be
     * executed.
     */
    @Test
    public void testMixedTainted_3() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(0, taintedString.length(), new IASTaintSource("dummy", 1234)));// everything is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since a sql syntax and attribute values
        // are tainted, the sanatization should be aborted and the query NOT executed.
        // -> return null
        assertNull(rs);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_1() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE TRUE=TRUE";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 3, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));// RU of second TRUE is
                                                                                                // tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since only a attribute value, more
        // specifically only a part of a attribute value, is tainted query should be
        // executed as prepared statement
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("Rome", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_2() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE ID=123";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 2, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));// 2 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since only a attribute value, more
        // specifically only a part of a attribute value, is tainted query should be
        // executed as prepared statement but resultset is empty because ID does not
        // exist
        assertNotNull(rs);
        assertFalse(rs.next());
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_3() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE ID=123 OR ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 10, taintedString.length() - 9, new IASTaintSource("dummy", 1234)));// 2 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_4() throws SQLException {
        Connection con = this.getConnection();
        String s = "12";
        String taintedString = "SELECT City FROM Students WHERE ID=" + s + "3 OR ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 11, taintedString.length() - 9, new IASTaintSource("dummy", 1234)));// 12 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_5() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE ID=1234 OR ID=1";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 11, taintedString.length() - 9, new IASTaintSource("dummy", 1234)));// 23 is tainted
        ranges.add(new IASTaintRange(taintedString.length() - 9, taintedString.length() - 8, new IASTaintSource("dummy", 1234)));// 4 is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_6() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE City='Rome'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 3, taintedString.length() - 1, new IASTaintSource("dummy", 1234)));// me is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("Rome", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_7() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE City='New York'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ranges.add(new IASTaintRange(taintedString.length() - 5, taintedString.length() - 3, new IASTaintSource("dummy", 1234)));// Yo is tainted
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is NOT empty
     */
    @Test
    public void testEmptyTaintRange_1() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT * FROM Students WHERE City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset not empty because there is a match for city=london
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_2() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT * FROM Students WHERE City='Eden'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset is empty because there is no match for city='Eden'
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_3() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students ORDER BY City";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset not empty because the table Students is not empty
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("2", rs.getString("ID"));
        assertEquals("Rome", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("3", rs.getString("ID"));
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_4() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT s.ID, s.City FROM Students s INNER JOIN Students t ON s.City=t.City WHERE t.City='London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset not empty because the table Students is not empty
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_5() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students GROUP BY Students.City";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset not empty because the table Students is not empty
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("Rome", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_6() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students GROUP BY Students.City, Students.ID HAVING ID<2";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset not empty because the table Students is not empty
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_7() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset not empty because the table Students is not empty
        assertTrue(rs.next());
        assertEquals(1, rs.getInt("Sum_ID"));
        assertTrue(rs.next());
        assertEquals(2, rs.getInt("Sum_ID"));
        assertTrue(rs.next());
        assertEquals(3, rs.getInt("Sum_ID"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_8() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT TOP 1 ID FROM Students";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertNotNull(rs);
        // resultset not empty because the table Students is not empty
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_9() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "CREATE TABLE Studentss AS SELECT * FROM Students;";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        // BUT create statements never return a result set -> rs=null
        assertNull(rs);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_10() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "ALTER TABLE Students ADD Age INT DEFAULT 20 NOT NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed and
        // no result set is generated
        assertNull(rs);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_11() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "DROP TABLE Profs";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        // BUT object not found because table with this name does not exist
        assertNull(rs);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_12() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "TRUNCATE TABLE Profs";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        // BUT object not found because table with this name does not exist
        assertNull(rs);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_13() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID, City FROM Students WHERE City LIKE 'London'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertEquals("London", rs.getString("City"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_14() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID IS NULL";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_15() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT ID FROM Students WHERE ID IN(1,2)";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);

        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("1", rs.getString("ID"));
        assertTrue(rs.next());
        assertEquals("2", rs.getString("ID"));
        assertFalse(rs.next());
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_16() throws SQLException {
        Connection con = this.getConnection();
        String taintedString = "SELECT City FROM Students WHERE City='London' OR City='St. John''s'";
        List<IASTaintRange> ranges = new ArrayList<>();
        ResultSet rs = Sanitization.sanitizeAndExecuteQuery(taintedString, ranges, con);
        // this query will cause problems due to the apostrophe in the city name.
        // this is to be expected
        assertNotNull(rs);
        assertTrue(rs.next());
        assertEquals("London", rs.getString("City"));
        assertTrue(rs.next());
        assertEquals("St. John's", rs.getString("City"));
        assertFalse(rs.next());
    }
}
