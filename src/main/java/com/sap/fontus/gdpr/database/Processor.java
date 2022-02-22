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
            System.out.println("connected");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(this.catalog, null, null, new String[]{"TABLE"});

            /*ResultSetMetaData rsm = tables.getMetaData();
            for(int i = 0; i < rsm.getColumnCount(); i++) {
                System.out.println(rsm.getColumnName(i+1));
            }*/


            while (tables.next()) {

                String cat = tables.getString(1);
                String schema = tables.getString(2);
                String name = tables.getString(3);
                String type = tables.getString(4);
                System.out.printf("%s.%s.%s - %s%n", cat, schema, name, type);
                this.gatherer.beginTable(cat, name);
                this.processTable(conn, cat, name);
                this.gatherer.endTable();
            }
            System.out.println("done");
        }
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
                        throw new IllegalStateException(String.format("In %s.%s the column %s at index %d is not a taint column!%n", catalog, table, columnName, i));
                    }
                    String originalColumnName = metaData.getColumnName(i - 1);
                    String taintValue = rs.getString(i);
                    if (taintValue == null || taintValue.equals("0")) {
                        this.gatherer.untaintedColumn(i - 1, columnName, rs.getObject(i - 1));
                    } else {
                        IASTaintInformationable tis = Utils.parseTaint(taintValue);
                        this.gatherer.taintedColumn(i - 1, columnName, rs.getString(i - 1), tis);
                    }
                }
            }
        }
    }
}
