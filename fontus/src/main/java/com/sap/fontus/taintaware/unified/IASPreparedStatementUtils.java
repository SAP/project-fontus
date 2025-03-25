package com.sap.fontus.taintaware.unified;

import com.sap.fontus.sql.driver.IASPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class IASPreparedStatementUtils {

    private IASPreparedStatementUtils() {
    }

    public static void setString(PreparedStatement ps, int idx, IASString value) throws SQLException {
        if (ps instanceof IASPreparedStatement statement) {
            statement.setString(idx, value);
        } else if (ps.isWrapperFor(IASPreparedStatement.class)) {
            IASPreparedStatement iasPreparedStatement = ps.unwrap(IASPreparedStatement.class);
            iasPreparedStatement.setString(idx, value);
        } else {
            if (value != null) {
                ps.setString(idx, value.getString());
            } else {
                ps.setString(idx, null);
            }
        }
    }

    public static void setNString(PreparedStatement ps, int idx, IASString value) throws SQLException {
        if (ps instanceof IASPreparedStatement statement) {
            statement.setNString(idx, value);
        } else if (ps.isWrapperFor(IASPreparedStatement.class)) {
            IASPreparedStatement iasPreparedStatement = ps.unwrap(IASPreparedStatement.class);
            iasPreparedStatement.setNString(idx, value);
        } else {
            if (value != null) {
                ps.setNString(idx, value.getString());
            } else {
                ps.setNString(idx, null);
            }
        }
    }

    public static void setObject(PreparedStatement ps, int parameterIndex, Object x) throws SQLException {
        if (ps instanceof IASPreparedStatement statement) {
            statement.setTObject(parameterIndex, x);
        } else if (ps.isWrapperFor(IASPreparedStatement.class)) {
            IASPreparedStatement iasPreparedStatement = ps.unwrap(IASPreparedStatement.class);
            iasPreparedStatement.setTObject(parameterIndex, x);
        } else {
            ps.setObject(parameterIndex, x);
        }
    }

    public static void setObject(PreparedStatement ps, int parameterIndex, Object x, int targetSqlType) throws SQLException {
        if (ps instanceof IASPreparedStatement statement) {
            statement.setTObject(parameterIndex, x, targetSqlType);
        } else if (ps.isWrapperFor(IASPreparedStatement.class)) {
            IASPreparedStatement iasPreparedStatement = ps.unwrap(IASPreparedStatement.class);
            iasPreparedStatement.setTObject(parameterIndex, x, targetSqlType);
        } else {
            ps.setObject(parameterIndex, x, targetSqlType);
        }
    }

    public static void setObject(PreparedStatement ps, int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        if (ps instanceof IASPreparedStatement statement) {
            statement.setTObject(parameterIndex, x, targetSqlType, scaleOrLength);
        } else if (ps.isWrapperFor(IASPreparedStatement.class)) {
            IASPreparedStatement iasPreparedStatement = ps.unwrap(IASPreparedStatement.class);
            iasPreparedStatement.setTObject(parameterIndex, x, targetSqlType, scaleOrLength);
        } else {
            ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        }
    }

    public static boolean useStatementFacade(Object o) {
        return false;
    }
}
