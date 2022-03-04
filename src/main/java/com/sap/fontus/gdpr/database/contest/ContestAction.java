package com.sap.fontus.gdpr.database.contest;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.sql.driver.Utils;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.utils.Pair;

import java.sql.*;

public class ContestAction implements AutoCloseable {
    private final Connection connection;

    public ContestAction(String host, String userName, String password, String catalog) throws SQLException {
        Configuration.setTestConfig(TaintMethod.RANGE);
        String connectionString = String.format("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=UTF-8", host, catalog);
        this.connection = DriverManager.getConnection(connectionString, userName, password);
    }

    public boolean canContest(SupportedApplication app, ContestDataType type, String id) throws SQLException {
        Location loc = getLocation(app, type);
        Pair<String, String> taint = this.fetchTaintValue(loc, id);
        IASTaintInformationable taintInformationable = Utils.parseTaint(taint.y);
        com.sap.fontus.gdpr.Utils.markContested(taintInformationable);
        IASString v = IASString.fromString(taint.x);
        v.setTaint(taintInformationable);
        String newTaint = Utils.serializeTaints(v);

        return this.updateTaintValue(loc, id, newTaint);
    }

    private boolean updateTaintValue(Location location, String id, String taint) throws SQLException {
        try (PreparedStatement ps = this.connection.prepareStatement(String.format("UPDATE %s SET %s = ? where %s = ?", location.getTable(), location.getTaintColumn(), location.getIdColumn()))) {
            ps.setObject(1, taint);
            ps.setObject(2, id);
            return ps.executeUpdate() > 0;
        }
    }
    private Pair<String, String> fetchTaintValue(Location location, String id) throws SQLException {
        try (PreparedStatement ps = this.connection.prepareStatement(String.format("SELECT %s, %s from %s where %s = ?", location.getColumn(), location.getTaintColumn(), location.getTable(), location.getIdColumn()))) {
            ps.setObject(1, id);
            try(ResultSet rs = ps.executeQuery()) {
                if(!rs.next()) {
                    throw new IllegalStateException(String.format("No entry found in %s.%s for %s", location.getTable(), location.getColumn(), id));
                }
                String value = rs.getString(1);
                String taint = rs.getString(2);
                System.out.printf("%s -> %s%n", value, taint);
                return new Pair<>(value, taint);
            }
        }
    }

    private static Location getLocation(SupportedApplication app, ContestDataType type) {
        switch (app) {
            case OLAT:
                switch(type) {
                    case EMAIL:
                        return new Location("user_id", "o_user", "u_email");
                    case FIRSTNAME:
                        return new Location("user_id", "o_user", "u_firstname");
                    case LASTNAME:
                        return new Location("user_id", "o_user", "u_lastname");
                    default:
                        throw new IllegalArgumentException(String.format("No mapping exists for %s -> %s", app, type));
                }
            default:
                throw new IllegalArgumentException(String.format("No mapping exists for %s -> %s", app, type));
        }
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
    }
}
