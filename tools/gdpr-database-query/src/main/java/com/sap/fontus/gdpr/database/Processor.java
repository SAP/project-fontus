package com.sap.fontus.gdpr.database;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.sql.driver.Utils;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.sql.*;

public class Processor {
    private final String userName;
    private final String password;
    private final String catalog;
    private final String connectionString;

    private final InformationGatherer gatherer;

    public Processor(String host, String userName, String password, String catalog, InformationGatherer gatherer) {
        Configuration.setTestConfig(TaintMethod.RANGE);
        this.userName = userName;
        this.password = password;
        this.catalog = catalog;
        this.connectionString = String.format("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=UTF-8", host, this.catalog);

        this.gatherer = gatherer;
    }

    public void run() throws SQLException {
        try (Connection conn = DriverManager.getConnection(this.connectionString, this.userName, this.password)
        ) {
            //System.out.println("connected");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(this.catalog, null, null, new String[]{"TABLE"/*, "VIEW"*/});

            /*ResultSetMetaData rsm = tables.getMetaData();
            for(int i = 0; i < rsm.getColumnCount(); i++) {
                System.out.println(rsm.getColumnName(i+1));
            }*/


            while (tables.next()) {

                String cat = tables.getString(1);
                String schema = tables.getString(2);
                String name = tables.getString(3);
                String type = tables.getString(4);
                //System.out.printf("%s.%s.%s - %s%n", cat, schema, name, type);
                int size = getTableSize(conn, cat, name);
                this.gatherer.beginTable(cat, name);
                this.gatherer.tableSize(size);
                this.processTable(conn, cat, name);
                this.gatherer.endTable();
            }
            //System.out.println("done");
        }
    }

    private int getTableSize(Connection conn, String catalog, String table) {
        int size = -1;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT (DATA_LENGTH + INDEX_LENGTH) AS SIZE FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?");
            ps.setString(1, catalog);
            ps.setString(2, table);
            ResultSet rs = ps.executeQuery();
            rs.next();
            size = rs.getInt("SIZE");
        } catch (SQLException e) {
            System.out.println("Exception computing table size: " + e.getMessage());
        }
        return size;
    }

    private void processTable(Connection conn, String catalog, String table) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(String.format("SELECT * from %s.%s", catalog, table)); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metaData = ps.getMetaData();
            int columnCount = metaData.getColumnCount();
            assert columnCount % 2 == 0;
            int row = 0;
            while (rs.next()) {
                row++;
                this.gatherer.nextRow();
                for (int i = 2; i <= columnCount; i += 2) {
                    String columnName = metaData.getColumnName(i);
                    if (!columnName.startsWith("__taint__")) {


                        int columnIndex = i - 1;
                        String originalColumnName = metaData.getColumnName(columnIndex);
                        String taintValue = rs.getString(i);
                        String columnType = metaData.getColumnTypeName(columnIndex);
                        if (taintValue == null || "0".equals(taintValue)) {
                            this.gatherer.untaintedColumn(columnIndex, originalColumnName, columnType, rs.getObject(columnIndex));
                        } else {
                            IASTaintInformationable tis = Utils.parseTaint(taintValue);
                            this.gatherer.taintedColumn(columnIndex, originalColumnName, columnType, rs.getString(columnIndex), tis);
                        }
                    }
                }
            }
        }
    }
}
