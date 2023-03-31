package com.sap.fontus.sql;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.sql.driver.ConnectionWrapper;
import com.sap.fontus.sql.driver.PreparedStatementWrapper;
import com.sap.fontus.sql.tainter.ParameterType;
import com.sap.fontus.sql.tainter.QueryParameters;
import com.sap.fontus.sql.tainter.StatementTainter;
import com.sap.fontus.sql.tainter.TaintAssignment;
import com.sap.fontus.taintaware.unified.IASPreparedStatementUtils;
import com.sap.fontus.taintaware.unified.IASString;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statements;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.EnableJUnit4MigrationSupport;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed", "CallToDriverManagerGetConnection", "SqlResolve", "JDBCPrepareStatementWithNonConstantString", "JDBCExecuteWithNonConstantString"})
@EnableJUnit4MigrationSupport
class PreparedStatementTests {

    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }
    
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
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, 2, ParameterType.QUERY_SUBSELECT), first);
        TaintAssignment second = parameters.computeAssignment(2);
        assertEquals(new TaintAssignment(2, 3, ParameterType.WHERE), second);

        ResultSet rss = ps.executeQuery();
    }

    @Test
    @Ignore("Known issue with SQL rewriter")
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
            if(i == 1) {
                continue;
            }
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
    void testSubselectWithAggregration() throws Exception {
        String query = "SELECT first_name, (select count(*) from contacts) as cnt  from contacts where id < ?\n;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.WHERE), first);
        ps.setInt(1, 5);

        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testParameterInJoinClause() throws Exception {
        String query = "SELECT c.first_name, c.last_name from contacts c where c.id in (select ci.id from contacts ci join meta m on ci.id = m.contact_id and ci.id = ?)\n;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.WHERE), first);
        ps.setInt(1, 5);

        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testParameterInJoinClause2() throws Exception {
        String sql = "select  fin_statement.course_repo_key,  count(fin_statement.id),  sum(case when fin_statement.passed=1 then 1 else 0 end) as num_of_passed,  sum(case when fin_statement.passed=0 then 1 else 0 end) as num_of_failed,  avg(fin_statement.score) from o_as_eff_statement fin_statement where fin_statement.id in (select sg_statement.id  from o_repositoryentry sg_re inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role in ('owner','coach'))  inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant') inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)  where sg_coach.fk_identity_id=? and sg_re.status  in ('coachpublished','published','closed') union select sg_statement.id  from o_repositoryentry sg_re  inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id and owngroup.r_defgroup=1)  inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=owngroup.fk_group_id and sg_coach.g_role ='owner' ) inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant')  inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)  where sg_coach.fk_identity_id=? and sg_re.status  in ('coachpublished','published','closed')) group by fin_statement.course_repo_key \n";
        StatementTainter tainter = new StatementTainter();
        Statements stmts=  CCJSqlParserUtil.parseStatements(sql);
        stmts.accept(tainter);
        //System.out.println((stmts.toString().trim()));
        QueryParameters parameters = tainter.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.SUBSELECT_WHERE), first);
        TaintAssignment second = parameters.computeAssignment(2);
        assertEquals(new TaintAssignment(2, 2, ParameterType.SUBSELECT_WHERE), second);
    }
    @Test
    void testStringIndexOutOfBounds() throws Exception {
    String sql = "select a.a, (select count(*)>0 from b cross join c where b.id=c.id and c.id=?) as xxx  from a where a.id=?\n";
        StatementTainter tainter = new StatementTainter();
        Statements stmts=  CCJSqlParserUtil.parseStatements(sql);
        stmts.accept(tainter);
        //System.out.println((stmts.toString().trim()));
        QueryParameters parameters = tainter.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, 2, ParameterType.QUERY_SUBSELECT), first);
        TaintAssignment second = parameters.computeAssignment(2);
        assertEquals(new TaintAssignment(2, 3, ParameterType.WHERE), second);
    }

    @Test
    void testparameterInLimit() throws Exception {
        String sql = "select * from contacts where id < ? limit ?, ?\n";
        StatementTainter tainter = new StatementTainter();
        Statements stmts=  CCJSqlParserUtil.parseStatements(sql);
        stmts.accept(tainter);
        //System.out.println((stmts.toString().trim()));
        QueryParameters parameters = tainter.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1,  ParameterType.WHERE), first);
        TaintAssignment second = parameters.computeAssignment(2);
        assertEquals(new TaintAssignment(2, 2, ParameterType.WHERE), second);
        TaintAssignment third = parameters.computeAssignment(3);
        assertEquals(new TaintAssignment(3, 3, ParameterType.WHERE), third);
    }

    @Test
    void testUpdateWithConcat() throws Exception {
        String sql = "update o_property set textvalue=concat(textvalue, ?), lastmodified=? where identity=? and resourcetypename=? and resourcetypeid=? and category=? and name=?\n";
        StatementTainter tainter = new StatementTainter();
        Statements stmts=  CCJSqlParserUtil.parseStatements(sql);
        stmts.accept(tainter);
        //System.out.println((stmts.toString().trim()));
        QueryParameters parameters = tainter.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertTrue(true);
    }
    // UPDATE jforum_forums SET forum_topics = forum_topics + ? WHERE forum_id = ?
    @Test
    void testUpdateThatIncrementsByStepSize() throws Exception {
        //String sql = "Update foo set a = ? where b = ?\n";
        String sql = "UPDATE jforum_forums SET c = ?, forum_topics = forum_topics + ? WHERE forum_id = ?\n";
        StatementTainter tainter = new StatementTainter();
        Statements stmts=  CCJSqlParserUtil.parseStatements(sql);
        stmts.accept(tainter);
        //System.out.println((stmts.toString().trim()));
        QueryParameters parameters = tainter.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertNotNull(first);
        TaintAssignment second = parameters.computeAssignment(2);
        assertNotNull(second);
        TaintAssignment third = parameters.computeAssignment(3);
        assertNotNull(third);
    }
    @Test
    void testInsertNullParam() throws Exception {
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
        IASString untainted = new IASString("foo");
        IASString ne = null;
        IASString tainted = new IASString("bar");
        tainted.setTaint(true);
        IASPreparedStatementUtils.setString(ps, 2, untainted);
        IASPreparedStatementUtils.setString(ps, 3, ne);
        IASPreparedStatementUtils.setString(ps, 3, tainted);
        ps.setString(5, "1234");
        for(int i = 1; i <= parameters.getParameterCount(); i++) {
            TaintAssignment expected = assignments[i-1];
            TaintAssignment actual = parameters.computeAssignment(i);
            assertEquals(expected, actual);
        }
        ResultSet rss = ps.executeQuery();
    }
    @Test
    void testSubselectWithJoin() throws Exception {
        String query = "SELECT first_name, (select count(*) from meta join contacts c where meta.contact_id = c.id) FROM contacts WHERE id = ?\n;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.WHERE), first);
        ps.setInt(1, 1);

        ResultSet rss = ps.executeQuery();
    }

    // SELECT person0_.person_id AS person_i1_74_0_, person0_.uuid AS uuid2_74_0_, person0_.gender AS gender3_74_0_, person0_.birthdate AS birthdat4_74_0_, person0_.birthdate_estimated AS birthdat5_74_0_, person0_.birthtime AS birthtim6_74_0_, person0_.dead AS dead7_74_0_, person0_.death_date AS death_da8_74_0_, person0_.deathdate_estimated AS deathdat9_74_0_, person0_.cause_of_death AS cause_o10_74_0_, person0_.creator AS creator11_74_0_, person0_.date_created AS date_cr12_74_0_, person0_.changed_by AS changed13_74_0_, person0_.date_changed AS date_ch14_74_0_, person0_.voided AS voided15_74_0_, person0_.voided_by AS voided_16_74_0_, person0_.date_voided AS date_vo17_74_0_, person0_.void_reason AS void_re18_74_0_, person0_.cause_of_death_non_coded AS cause_o19_74_0_, person0_1_.patient_id AS patient_1_68_0_, person0_1_.creator AS creator10_68_0_, person0_1_.date_created AS date_cre2_68_0_, person0_1_.changed_by AS changed_3_68_0_, person0_1_.date_changed AS date_cha4_68_0_, person0_1_.voided AS voided5_68_0_, person0_1_.voided_by AS voided_b6_68_0_, person0_1_.date_voided AS date_voi7_68_0_, person0_1_.void_reason AS void_rea8_68_0_, person0_1_.allergy_status AS allergy_9_68_0_, CASE WHEN EXISTS (SELECT * FROM patient p WHERE p.patient_id = person0_.person_id) THEN 1 ELSE 0 END AS formula1_0_, CASE WHEN person0_1_.patient_id IS NOT NULL THEN 1 WHEN person0_.person_id IS NOT NULL THEN 0 END AS clazz_0_ FROM person person0_ LEFT OUTER JOIN patient person0_1_ ON person0_.person_id = person0_1_.patient_id WHERE person0_.person_id = ?
    @Test
    void testSelectInCase() throws Exception {
        String query = "SELECT first_name, last_name, CASE WHEN EXISTS (SELECT info FROM meta m WHERE m.contact_id = id) THEN 1 ELSE 0 END AS has_info FROM contacts WHERE id = ?\n;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.WHERE), first);
        ps.setInt(1, 1);

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
        for(int i = 1; i <= parameters.getParameterCount(); i++) {
            TaintAssignment expected = assignments[i-1];
            TaintAssignment actual = parameters.computeAssignment(i);
            assertEquals(expected, actual);
        }
        ResultSet rss = ps.executeQuery();
    }

    // select members2_.fk_identity_id as col_0_0_ from o_gp_business businessgr0_ inner join o_bs_group groupimpl1_ on businessgr0_.fk_group_id=groupimpl1_.id inner join o_bs_group_member members2_ on groupimpl1_.id=members2_.fk_group_id inner join o_bs_group_member members3_ on groupimpl1_.id=members3_.fk_group_id and (members3_.fk_identity_id=?) where businessgr0_.ownersintern=1 and members2_.g_role='coach' or businessgr0_.participantsintern=1 and members2_.g_role='participant'
    @Test
    void testParamInJoin() throws SQLException {
        String query = "select id, first_name, last_name from contacts inner join meta on meta.contact_id and meta.info = ?;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.WHERE), first);
        ps.setString(1, "bar");

        ResultSet rss = ps.executeQuery();
    }

    @Test
    void deleteTest() throws Exception {
        String query = "delete from contacts where id=?;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);


        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        ps.setLong(1, 1L);
        TaintAssignment[] assignments = {
                new TaintAssignment(1, 1, ParameterType.WHERE),
        };
        for(int i = 1; i <= parameters.getParameterCount(); i++) {
            TaintAssignment expected = assignments[i-1];
            TaintAssignment actual = parameters.computeAssignment(i);
            assertEquals(expected, actual);
        }
        ResultSet rss = ps.executeQuery();
    }

    // select categoryim0_.CATEGORY_ID as CATEGORY1_26_, categoryim0_.ACTIVE_END_DATE as ACTIVE_E2_26_, categoryim0_.ACTIVE_START_DATE as ACTIVE_S3_26_, categoryim0_.ARCHIVED as ARCHIVED4_26_, categoryim0_.DEFAULT_PARENT_CATEGORY_ID as DEFAULT21_26_, categoryim0_.DESCRIPTION as DESCRIPT5_26_, categoryim0_.DISPLAY_TEMPLATE as DISPLAY_6_26_, categoryim0_.EXTERNAL_ID as EXTERNAL7_26_, categoryim0_.FULFILLMENT_TYPE as FULFILLM8_26_, categoryim0_.INVENTORY_TYPE as INVENTOR9_26_, categoryim0_.LONG_DESCRIPTION as LONG_DE10_26_, categoryim0_.META_DESC as META_DE11_26_, categoryim0_.META_TITLE as META_TI12_26_, categoryim0_.NAME as NAME13_26_, categoryim0_.OVERRIDE_GENERATED_URL as OVERRID14_26_, categoryim0_.PRODUCT_DESC_PATTERN_OVERRIDE as PRODUCT15_26_, categoryim0_.PRODUCT_TITLE_PATTERN_OVERRIDE as PRODUCT16_26_, categoryim0_.ROOT_DISPLAY_ORDER as ROOT_DI17_26_, categoryim0_.TAX_CODE as TAX_COD18_26_, categoryim0_.URL as URL19_26_, categoryim0_.URL_KEY as URL_KEY20_26_ from BLC_CATEGORY categoryim0_ where categoryim0_.ARCHIVED=? or categoryim0_.ARCHIVED is null order by categoryim0_.CATEGORY_ID asc limit ?

    @Test
    void testParameterizedLimit() throws SQLException {
        String query = "select id, first_name, last_name from contacts where id < ? order by id limit ? ;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        MockPreparedStatement mps = ps.unwrap(MockPreparedStatement.class);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment first = parameters.computeAssignment(1);
        assertEquals(new TaintAssignment(1, 1, ParameterType.WHERE), first);
        TaintAssignment second = parameters.computeAssignment(2);
        assertEquals(new TaintAssignment(2, 2, ParameterType.WHERE), second);
        ps.setInt(1, 5);
        ps.setInt(2, 2);

        ResultSet rss = ps.executeQuery();
    }

    @Test
    void testNestedQueryInUpdateWhere() throws Exception {
        String query = "update contacts set first_name = 'Elda', last_name = (select info from meta where contact_id = ?) where id = ?;";
        Connection mc = new MockConnection(this.conn);
        Connection c = ConnectionWrapper.wrap(mc);
        PreparedStatement ps = c.prepareStatement(query);
        PreparedStatementWrapper unwrapped = ps.unwrap(PreparedStatementWrapper.class);
        QueryParameters parameters = unwrapped.getParameters();
        TaintAssignment[] assignments = {
                new TaintAssignment(1, 1, ParameterType.WHERE),
                new TaintAssignment(2, 2, ParameterType.WHERE),
        };
        for(int i = 1; i <= parameters.getParameterCount(); i++) {
            TaintAssignment expected = assignments[i-1];
            TaintAssignment actual = parameters.computeAssignment(i);
            assertEquals(expected, actual);
        }
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
