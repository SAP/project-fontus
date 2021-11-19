package com.sap.fontus.sanitizer;

import com.alibaba.druid.sql.parser.Token;
import com.sap.fontus.taintaware.shared.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * JUnit test for sanitization of sql statements.
 */
public class SqlSanitizationTest {

    private static final Token[] taintableTokens = {
            Token.IDENTIFIER,
            Token.LITERAL_INT,
            Token.LITERAL_FLOAT,
            Token.LITERAL_HEX,
            Token.LITERAL_CHARS,
            Token.LITERAL_NCHARS,
            Token.LITERAL_PATH,
            Token.LITERAL_ALIAS
    };

    private static final Token[] tokensNoIdentifier = {
            Token.LITERAL_INT,
            Token.LITERAL_FLOAT,
            Token.LITERAL_HEX,
            Token.LITERAL_CHARS,
            Token.LITERAL_NCHARS,
            Token.LITERAL_PATH,
            Token.LITERAL_ALIAS
    };

    public SqlSanitizationTest() {
    }

    private static IASTaintMetadata source = new IASBasicMetadata(IASTaintSourceRegistry.getInstance().getOrRegisterTaintSource("dummy"));
    /**
     * One complete attribute value is tainted. Wouldn't have changed syntax of
     * query.
     */
    @Test
    public void testAttributeValueTainted_1() {
        String taintedString = "SELECT ID, City FROM Students WHERE City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        // London is tainted, i.e. the '' are NOT included in the taint
        ranges.setTaint(taintedString.length() - 7, taintedString.length() - 1, source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * One complete attribute value is tainted. Interpreted as it is and not as part
     * of syntax and therefore converted into the string London.
     */
    @Test
    public void testAttributeValueTainted_2() {
        String taintedString = "SELECT ID, City FROM Students WHERE City=4c6f6e646f6e"; // hex for London
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 12, taintedString.length(), source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * One complete attribute value is tainted. Would have changed syntax of query
     * because hex would have been interpreted as part of command.
     */
    @Test
    public void testAttributeValueTainted_3() {
        String badInput = "' OR 1=1--";
        String taintedString = "SELECT ID, City FROM Students WHERE City='" + badInput + "'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        // >>' OR 1=1--<< is tainted
        ranges.setTaint(taintedString.length() - 11, taintedString.length() - 1, source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * One complete attribute value is tainted. Interpreted as integer.
     */
    @Test
    public void testAttributeValueTainted_4() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 1, taintedString.length(), source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * One complete attribute value is tainted. Would have changed syntax of query
     * because hex would have been interpreted as part of command, not as integer.
     */
    @Test
    public void testAttributeValueTainted_5() {
        String badInput = "31204f5220313d312d2d"; // is hex representation of 1 OR 1=1--
        String taintedString = "SELECT ID, City FROM Students WHERE ID=" + badInput;
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 20, taintedString.length(), source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * One complete attribute value is tainted, other attributes are not tainted.
     */
    @Test
    public void testAttributeValueTainted_6() {
        String taintedString = "SELECT ID, City FROM Students WHERE 1=1 AND ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 19, taintedString.length() - 18, source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Not all attribute values are tainted
     */
    @Test
    public void testAttributeValueTainted_7() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        // London is tainted, i.e. the '' are NOT included in the taint
        ranges.setTaint(taintedString.length() - 7, taintedString.length() - 1, source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);
        
        assertFalse(detected);
    }

    /**
     * Multiple complete attribute values are tainted. Resultset is not empty.
     */
    @Test
    public void testAttributeValueTainted_8() {
        String taintedString = "SELECT ID, City FROM Students WHERE City='London' AND ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        // London is tainted
        ranges.setTaint(taintedString.length() - 16, taintedString.length() - 10, source);
        // 1 is tainted
        ranges.setTaint(taintedString.length() - 1, taintedString.length(), source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Multiple complete attribute values are tainted. Resultset is not empty.
     */
    @Test
    public void testAttributeValueTainted_9() {
        String badInput = "' OR 1=1--";
        String taintedString = "SELECT ID, City FROM Students WHERE City='" + badInput + "' AND ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        // badInput is tainted
        ranges.setTaint(taintedString.length() - 20, taintedString.length() - 10, source);
        // 1 is tainted
        ranges.setTaint(taintedString.length() - 1, taintedString.length(), source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * Tainted attribute value
     */
    @Test
    public void testAttributeValueTainted_10() {
        String taintedString = "SELECT TOP 1 ID FROM Students";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(11, 12, source); // 1 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * Tainted attribute value
     */
    @Test
    public void testAttributeValueTainted_11() {
        String taintedString = "SELECT ID FROM Students WHERE ID BETWEEN 1 AND 2";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 7, taintedString.length() - 6, source); // 1 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);
        
        assertFalse(detected);
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_12() {
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 11, taintedString.length() - 9, source); // 20 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_13() {
        String taintedString = "SELECT ID, City FROM Students WHERE City LIKE 'London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 7, taintedString.length() - 1, source); // London is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);
        
        assertFalse(detected);
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_14() {
        String taintedString = "SELECT ID FROM Students WHERE ID IN(1,2)";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 2, taintedString.length() - 1, source); // 2 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Tainted value
     */
    @Test
    public void testAttributeValueTainted_15() {
         String taintedString = "SELECT City FROM Students WHERE City='\' OR 1=1\''";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 10, taintedString.length() - 1, source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * Tainted static value
     */
    @Test
    public void testAttributeValueTainted_16() {
        String taintedString = "SELECT City FROM Students WHERE TRUE=TRUE";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 4, taintedString.length(), source);// second TRUE is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * Tainted value with apostrophe in text. Thus it is a user input, the query
     * itself should have worked before applying changes due to the
     * sanitizeAndExecuteQuery method.
     */
    @Test
    public void testAttributeValueTainted_17() {
        String taintedString = "SELECT City FROM Students WHERE City='St. John\'s' OR City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        // St. John's is tainted
        ranges.setTaint(taintedString.length() - 28, taintedString.length() - 18, source);
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * Tainted value. But apostrophe in the another attribute text since it is not a
     * user input, the query itself should have worked before applying changes due
     * to the sanitizeAndExecuteQuery method. If it didn't work before, it won't
     * now. Handling this kind of problem is the responsibility of the dbs owner or
     * creator of the sql query.
     */
    @Test
    public void testAttributeValueTainted_18() {
        String taintedString = "SELECT City FROM Students WHERE City='St. John''s' OR City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 7, taintedString.length() - 1, source);// London is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);
        // this query will cause problems due to the apostrophe in the city name.
        // this is to be expected
        assertFalse(detected);
    }

    /**
     * Tainted value. But apostrophe in the another attribute text since it is not a
     * user input, the query itself should have worked before applying changes due
     * to the sanitizeAndExecuteQuery method. If it didn't work before, it won't
     * now. Handling this kind of problem is the responsibility of the dbs owner or
     * creator of the sql query.
     */
    @Test
    public void testAttributeValueTainted_19() {
        String taintedString = "SELECT City FROM Students WHERE City='London' OR City='St. John''s'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 29, taintedString.length() - 23, source);// London is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);
        // this query will cause problems due to the apostrophe in the city name.
        // this is to be expected
        assertFalse(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_1() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(0, 6, source);// SELECT is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql command is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_2() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(16, 20, source);// FROM is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_3() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(30, 35, source);// WHERE is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_4() {
        String taintedString = "SELECT City FROM Students GROUP BY Students.City";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(26, 34, source);// GROUP BY is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_5() {
        String taintedString = "SELECT ID, City FROM Students GROUP BY Students.City, Students.ID HAVING ID<2";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(66, 72, source);// HAVING is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_6() {
        String taintedString = "SELECT ID, City FROM Students ORDER BY City";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(30, 38, source);// ORDER BY is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_7() {
        String taintedString = "SELECT s.ID, s.City FROM Students s INNER JOIN Students t ON s.City=t.City WHERE t.City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(36, 46, source);// INNER JOIN is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_8() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(41, 44, source);// AND is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_9() {
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(7, 10, source);// SUM is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_10() {
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(15, 17, source);// AS is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_11() {
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 3, taintedString.length(), source);// ASC is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_12() {
        String taintedString = "SELECT TOP 1 ID FROM Students";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(7, 10, source);// TOP is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_13() {
        String taintedString = "SELECT ID FROM Students WHERE ID BETWEEN 1 AND 2";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 15, taintedString.length() - 8, source); // BETWEEN is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_14() {
        String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(0, 6, source); // CREATE is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as CREATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_15() {
         String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 8, taintedString.length() - 1, source); // INTEGER is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as CREATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertFalse(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_16() {
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 8, taintedString.length() - 5, source); // NOT is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_17() {
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 19, taintedString.length() - 12, source); // DEFAULT is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_18() {
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(21, 24, source); // ADD is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertFalse(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_19() {
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(0, 5, source); // ALTER is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_20() {
        String taintedString = "DROP TABLE Profs";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(0, 4, source); // DROP is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as DROP,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_21() {
        String taintedString = "TRUNCATE TABLE Profs";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(0, 8, source); // TRUNCATE is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as TRUNCATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_22() {
        String taintedString = "SELECT ID FROM Students WHERE ID IS NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 11, taintedString.length(), source); // IS NOT NULL is
                                                                                                  // tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // sql keyword is tainted -> sanatization is aborted -> query NOT executed
        // -> return null
        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_23() {
        String taintedString = "SELECT ID FROM Students WHERE ID IS NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 7, taintedString.length(), source); // IS NULL is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_24() {
        String taintedString = "SELECT ID, City FROM Students WHERE City LIKE 'London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 13, taintedString.length() - 9, source); // LIKE is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL commands/keywords are tainted. Query should NOT be executed.
     */
    @Test
    public void testKeywordTainted_25() {
        String taintedString = "SELECT ID FROM Students WHERE ID IN(1,2)";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 7, taintedString.length() - 5, source); // IN is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertTrue(detected);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_1() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(21, 29, source);// Students is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, tokensNoIdentifier);

        // check if sanitization was performed, since a table name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertTrue(detected);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_2() {
        String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(13, 18, source); // Profs is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, tokensNoIdentifier);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_3() {
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(12, 20, source); // Students is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, tokensNoIdentifier);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_4() {
        String taintedString = "DROP TABLE Profs";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(11, 16, source); // Profs is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, tokensNoIdentifier);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as DROP,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL table name is tainted. Query should NOT be executed.
     */
    @Test
    public void testTableNameTainted_5() {
        String taintedString = "TRUNCATE TABLE Profs";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(15, 20, source); // Profs is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, tokensNoIdentifier);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as TRUNCATE,
        // hence this results in a NUllPointerException and execution is terminated
        assertTrue(detected);
    }

    /**
     * SQL attribute/column name is tainted. Used existing column name. Query should
     * NOT be executed.
     */
    @Test
    public void testColumnNameTainted_1() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(7, 9, source); // first ID is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed. It was due to taint.
        // "ID" is interpreted as string and not as column. Therefore first result
        // column only contains the string "ID" even though there is a column with the
        // name ID
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Used NOT existing column name. Query
     * should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_2() {
        String taintedString = "SELECT IDS, City FROM Students";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(7, 10, source); // first IDS is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed. It was due to taint.
        // "IDS" is interpreted as string and not as column. Therefore first result
        // column only contains the string "IDS" in each row
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_3() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 AND City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(36, 38, source); // second ID is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_4() {
        String taintedString = "SELECT ID, City FROM Students WHERE ID=1 GROUP BY City";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(50, 54, source);// second City is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since only a sql column name for
        // grouping the result was tainted, the string should be sanitized
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_5() {
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(11, 13, source);// first ID is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_6() {
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(18, 24, source);// Sum_ID is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_7() {
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 8, taintedString.length() - 4, source);// third City is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql column name is tainted, the
        // sanatization should be aborted and the query NOT executed. -> return null
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_8() {
        String taintedString = "CREATE TABLE Profs (Name VARCHAR(50), Age INTEGER)";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 12, taintedString.length() - 9, source); // Age is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertFalse(detected);
    }

    /**
     * SQL attribute/column name is tainted. Query should NOT be executed.
     */
    @Test
    public void testColumnNameTainted_9() {
        String taintedString = "ALTER TABLE Students ADD Age INTEGER DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(25, 28, source); // Age is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since something is tainted bind
        // variables are used. This is not allowed for DDL statements, such as ALTER,
        // hence this results in a NUllPointerException and execution is terminated
        assertFalse(detected);
    }

    /**
     * Part of SQL syntax and attribute value is tainted. Query should NOT be
     * executed.
     */
    @Test
    public void testMixedTainted_1() {
        String taintedString = "SELECT ID FROM Students WHERE ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 4, taintedString.length(), source);// ID=1 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql syntax and attribute values
        // are tainted, the sanatization should be aborted and the query NOT executed.
        // -> return null
        assertTrue(detected);
    }

    /**
     * Part of SQL syntax and attribute value is tainted. Query should NOT be
     * executed.
     */
    @Test
    public void testMixedTainted_2() {
        String taintedString = "SELECT ID FROM Students WHERE ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(7, 8, source);// first ID is tainted
        ranges.setTaint(taintedString.length() - 4, taintedString.length(), source);// ID=1 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql syntax and attribute values
        // are tainted, the sanatization should be aborted and the query NOT executed.
        // -> return null
        assertTrue(detected);
    }

    /**
     * Part of SQL syntax and attribute value is tainted. Query should NOT be
     * executed.
     */
    @Test
    public void testMixedTainted_3() {
           String taintedString = "SELECT ID FROM Students WHERE ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(0, taintedString.length(), source);// everything is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since a sql syntax and attribute values
        // are tainted, the sanatization should be aborted and the query NOT executed.
        // -> return null
        assertTrue(detected);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_1() {
        String taintedString = "SELECT City FROM Students WHERE TRUE=TRUE";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 3, taintedString.length() - 1, source);// RU of second TRUE is
                                                                                                // tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since only a attribute value, more
        // specifically only a part of a attribute value, is tainted query should be
        // executed as prepared statement
        assertTrue(detected);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_2() {
        String taintedString = "SELECT City FROM Students WHERE ID=123";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 2, taintedString.length() - 1, source);// 2 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since only a attribute value, more
        // specifically only a part of a attribute value, is tainted query should be
        // executed as prepared statement but resultset is empty because ID does not
        // exist
        assertFalse(detected);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_3() {
        String taintedString = "SELECT City FROM Students WHERE ID=123 OR ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 10, taintedString.length() - 9, source);// 2 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_4() {
        String s = "12";
        String taintedString = "SELECT City FROM Students WHERE ID=" + s + "3 OR ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 11, taintedString.length() - 9, source);// 12 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_5() {
        String taintedString = "SELECT City FROM Students WHERE ID=1234 OR ID=1";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 11, taintedString.length() - 9, source);// 23 is tainted
        ranges.setTaint(taintedString.length() - 9, taintedString.length() - 8, source);// 4 is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_6() {
        String taintedString = "SELECT City FROM Students WHERE City='Rome'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 3, taintedString.length() - 1, source);// me is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * Part of attribute value is tainted. Find rest of attribute value und execute
     * query correctly.
     */
    @Test
    public void testPartiallyTainted_7() {
        String taintedString = "SELECT City FROM Students WHERE City='New York'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        ranges.setTaint(taintedString.length() - 5, taintedString.length() - 3, source);// Yo is tainted
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is NOT empty
     */
    @Test
    public void testEmptyTaintRange_1() {
        String taintedString = "SELECT * FROM Students WHERE City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_2() {
        String taintedString = "SELECT * FROM Students WHERE City='Eden'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_3() {
        String taintedString = "SELECT ID, City FROM Students ORDER BY City";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
     }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_4() {
        String taintedString = "SELECT s.ID, s.City FROM Students s INNER JOIN Students t ON s.City=t.City WHERE t.City='London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_5() {
        String taintedString = "SELECT City FROM Students GROUP BY Students.City";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_6() {
        String taintedString = "SELECT ID, City FROM Students GROUP BY Students.City, Students.ID HAVING ID<2";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_7() {
        String taintedString = "SELECT SUM(ID) AS Sum_ID, City FROM Students GROUP BY Students.City ORDER BY City ASC";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_8() {
        String taintedString = "SELECT TOP 1 ID FROM Students";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_9() {
        String taintedString = "CREATE TABLE Studentss AS SELECT * FROM Students;";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        // BUT create statements never return a result set -> rs=null
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_10() {
        String taintedString = "ALTER TABLE Students ADD Age INT DEFAULT 20 NOT NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed and
        // no result set is generated
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_11() {
        String taintedString = "DROP TABLE Profs";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        // BUT object not found because table with this name does not exist
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_12() {
        String taintedString = "TRUNCATE TABLE Profs";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        // check if sanitization was performed, since taintRange is empty it is NOT
        // necessary to perform sanitization, hence the original query is executed
        // BUT object not found because table with this name does not exist
        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_13() {
        String taintedString = "SELECT ID, City FROM Students WHERE City LIKE 'London'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_14() {
        String taintedString = "SELECT ID FROM Students WHERE ID IS NULL";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_15() {
         String taintedString = "SELECT ID FROM Students WHERE ID IN(1,2)";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);

        assertFalse(detected);
    }

    /**
     * taintrange is empty, resultset is empty aswell
     */
    @Test
    public void testEmptyTaintRange_16() {
        String taintedString = "SELECT City FROM Students WHERE City='London' OR City='St. John''s'";
        IASTaintRanges ranges = new IASTaintRanges(taintedString.length());
        boolean detected = SQLChecker.sqlInjectionDetected(taintedString, ranges, taintableTokens);
        // this query will cause problems due to the apostrophe in the city name.
        // this is to be expected
        assertFalse(detected);
    }
}
