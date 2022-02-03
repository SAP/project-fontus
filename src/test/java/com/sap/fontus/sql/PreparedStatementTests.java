package com.sap.fontus.sql;

import com.sap.fontus.sql.driver.ConnectionWrapper;
import com.sap.fontus.sql.tainter.StatementTainter;
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
        this.executeUpdate("CREATE TABLE contacts ( contact_id INTEGER PRIMARY KEY, first_name TEXT NOT NULL, last_name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, phone TEXT NOT NULL UNIQUE);");
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
        String query = "select contact_id, first_name, last_name, (select info from meta where contact_id = ?) as meta from contacts where contact_id = ?;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        ps.setInt(1, 2);
        ps.setInt(2, 2);
        ResultSet rss = ps.executeQuery();
        //assertEquals("SELECT 'a' AS foo, '0' AS `__taint__foo`, (SELECT b FROM bla WHERE id < 5) AS bar, (SELECT `__taint__b` FROM bla WHERE id < 5) AS `__taint__bar`;", taintedStatement.trim());
    }

    private void executeUpdate(String sql) throws SQLException {
        Connection mc = ConnectionWrapper.wrap(this.conn);
        Statement st = mc.createStatement();
        st.executeUpdate(sql);
        st.close();
    }
}
