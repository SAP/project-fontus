package com.sap.fontus.taintaware.unified;

import com.sap.fontus.sql.driver.IASPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IASPreparedStatementUtils {

    public static void setString(PreparedStatement ps, int idx, IASString value) throws SQLException {
        if (ps instanceof IASPreparedStatement) {
            ((IASPreparedStatement) ps).setString(idx, value);
        } else {
            ps.setString(idx, value.getString());
        }
    }

    public static void setNString(PreparedStatement ps, int idx, IASString value) throws SQLException {
        if (ps instanceof IASPreparedStatement) {
            ((IASPreparedStatement) ps).setNString(idx, value);
        } else {
            ps.setNString(idx, value.getString());
        }
    }

    public static void setObject(PreparedStatement ps, int parameterIndex, Object x) throws SQLException {
        if (ps instanceof IASPreparedStatement) {
            ((IASPreparedStatement) ps).setTObject(parameterIndex, x);
        } else {
            ps.setObject(parameterIndex, x);
        }
    }

    public static void setObject(PreparedStatement ps, int parameterIndex, Object x, int targetSqlType) throws SQLException {
        if (ps instanceof IASPreparedStatement) {
            ((IASPreparedStatement) ps).setTObject(parameterIndex, x, targetSqlType);
        } else {
            ps.setObject(parameterIndex, x, targetSqlType);
        }
    }

    public static void setObject(PreparedStatement ps, int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        if (ps instanceof IASPreparedStatement) {
            ((IASPreparedStatement) ps).setTObject(parameterIndex, x, targetSqlType, scaleOrLength);
        } else {
            ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        }
    }

    public static boolean useStatementFacade(Object o) {
        return false;
    }
}
