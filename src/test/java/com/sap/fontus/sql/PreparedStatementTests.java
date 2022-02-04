package com.sap.fontus.sql;

import com.sap.fontus.sql.driver.ConnectionWrapper;
import com.sap.fontus.sql.driver.PreparedSelectStatementWrapper;
import com.sap.fontus.sql.driver.PreparedStatementWrapper;
import com.sap.fontus.sql.tainter.ParameterType;
import com.sap.fontus.sql.tainter.QueryParameters;
import com.sap.fontus.sql.tainter.StatementTainter;
import com.sap.fontus.sql.tainter.TaintAssignment;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PreparedStatementTests {
    private Connection conn;
    @BeforeEach
    void setup() throws SQLException {
        this.conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        this.executeUpdate("CREATE TABLE contacts ( id INTEGER PRIMARY KEY, first_name TEXT NOT NULL, last_name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, phone TEXT NOT NULL UNIQUE);");
        this.executeUpdate("INSERT INTO contacts VALUES(1, 'Lucy', 'Cechtelar', 'lucy@test.com', '1234')");
        this.executeUpdate("INSERT INTO contacts VALUES(2, 'Elda', 'Palumbo', 'elda@test.com', '12345')");
        this.executeUpdate("INSERT INTO contacts VALUES(3, 'Costanzo', 'Costa', 'cc@test.com', '123456')");
        this.executeUpdate("CREATE TABLE meta ( contact_id INTEGER, info TEXT);");
        this.executeUpdate("INSERT INTO meta VALUES(1, 'foo')");
        this.executeUpdate("INSERT INTO meta VALUES(2, 'bar')");
        this.executeUpdate("INSERT INTO meta VALUES(3, 'foobar')");
    }

    @AfterEach
    void teardown() throws SQLException {
        this.conn.close();
    }
    @Test
    void testNestedQueryWithWhere() throws SQLException {
        String query = "select id, first_name, last_name, (select info from meta where contact_id = ?) as meta from contacts where id = ?;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        ps.setInt(1, 2);
        ps.setInt(2, 2);
        PreparedSelectStatementWrapper unwrapped = ps.unwrap(PreparedSelectStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, 2, ParameterType.QUERY_SUBSELECT), first);
        TaintAssignment second = parameters.computeAssignment(2);
        assertEquals(new TaintAssignment(2, 3, ParameterType.WHERE), second);

        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testTupleUpdate() throws Exception {
        String query = "update contacts set (id) = (select id from contacts);";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        ResultSet rss = ps.executeQuery();

    }
    @Test
    void testInsertSubselect() throws Exception {
        String query = "INSERT INTO contacts VALUES(?, (select info from meta where contact_id = ?), ?, ?, ?)";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();

        TaintAssignment[] assignments = new TaintAssignment[5];
        for(int i = 0; i < 5; i++) {
            if(i == 1) continue;
            assignments[i] = new TaintAssignment(i+1, (i*2)+1, (i*2)+2, ParameterType.ASSIGNMENT);
        }
        assignments[1] = new TaintAssignment(2, 3, 4, ParameterType.ASSIGNMENT_SUBSELECT);
        ps.setInt(1, 5);
        ps.setInt(1, 2);
        ps.setString(3, "User");
        ps.setString(4, "test@user.com");
        ps.setString(5, "1234");
        for(int i = 1; i <= parameters.getParameterCount(); i++) {
            TaintAssignment expected = assignments[i-1];
            TaintAssignment actual = parameters.computeAssignment(i);
            assertEquals(expected, actual);
        }
        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testInsert() throws Exception {
        String query = "INSERT INTO contacts VALUES(?, ?, ?, ?, ?)";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();

        TaintAssignment[] assignments = new TaintAssignment[5];
        for(int i = 0; i < 5; i++) {
            assignments[i] = new TaintAssignment(i+1, (i*2)+1, (i*2)+2, ParameterType.ASSIGNMENT);
        }
        ps.setInt(1, 5);
        ps.setString(2, "Test");
        ps.setString(3, "User");
        ps.setString(4, "test@user.com");
        ps.setString(5, "1234");
        for(int i = 1; i <= parameters.getParameterCount(); i++) {
            TaintAssignment expected = assignments[i-1];
            TaintAssignment actual = parameters.computeAssignment(i);
            assertEquals(expected, actual);
        }
        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testNestedQueryInUpdate() throws Exception {
        String query = "update contacts set first_name = 'Elda', last_name = 'Palumbo' where id = (select contact_id from meta where info = ?) ;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        ps.setString(1, "bar");
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.SUBSELECT_WHERE), first);
        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testSubselectInWhere() throws Exception {
        String query = "update contacts set first_name = ?, last_name = ? where id = (select contact_id from meta where info = ?) ;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        ps.setString(1, "TEST");
        ps.setString(2, "USER");
        ps.setString(3, "bar");
        TaintAssignment[] assignments = {
                new TaintAssignment(1, 1, 2, ParameterType.ASSIGNMENT),
                new TaintAssignment(2, 3, 4, ParameterType.ASSIGNMENT),
                new TaintAssignment(3, 5, ParameterType.SUBSELECT_WHERE),

        };
        for(int i = 1; i <= parameters.getParameterCount(); i++) {
            TaintAssignment expected = assignments[i-1];
            TaintAssignment actual = parameters.computeAssignment(i);
            assertEquals(expected, actual);
        }
        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testSubselecstInWhereWithTrailingCondition() throws Exception {
        String query = "update contacts set first_name = ?, last_name = ? where id = (select contact_id from meta where info = ?) and first_name = ?;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        ps.setString(1, "TEST");
        ps.setString(2, "USER");
        ps.setString(3, "bar");
        ps.setString(4, "Elda");
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment[] assignments = {
                new TaintAssignment(1, 1, 2, ParameterType.ASSIGNMENT),
                new TaintAssignment(2, 3, 4, ParameterType.ASSIGNMENT),
                new TaintAssignment(3, 5, ParameterType.SUBSELECT_WHERE),
                new TaintAssignment(4, 6, ParameterType.WHERE),

        };
        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testNestedQueryInUpdateWhere() throws Exception {
        String query = "update contacts set first_name = 'Elda', last_name = (select info from meta where contact_id = ?) where id = ?;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        ps.setInt(1, 2);
        ps.setInt(2, 2);
        ResultSet rss = ps.executeQuery();
    }
    private void executeUpdate(String sql) throws SQLException {
        Connection mc = ConnectionWrapper.wrap(this.conn);
        Statement st = mc.createStatement();
        st.executeUpdate(sql);
        st.close();
    }
}
