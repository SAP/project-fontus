package com.sap.fontus.taintaware.unified;

import com.sap.fontus.sql.driver.IASResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class IASResultSetUtils {

    private IASResultSetUtils() {
    }

    public static Object getObject(ResultSet rs, int idx) throws SQLException {
        if (rs instanceof IASResultSet trs) {
            return trs.getTString(idx);
        } else if (rs.isWrapperFor(IASResultSet.class)) {
            IASResultSet iasResultSet = rs.unwrap(IASResultSet.class);
            return iasResultSet.getTObject(idx);
        } else {
            return rs.getObject(idx);
        }
    }

    public static Object getObject(ResultSet rs, IASString label) throws SQLException {
        if (rs instanceof IASResultSet trs) {
            return trs.getTString(label.getString());
        } else if (rs.isWrapperFor(IASResultSet.class)) {
            IASResultSet iasResultSet = rs.unwrap(IASResultSet.class);
            return iasResultSet.getTObject(label.getString());
        } else {
            return rs.getObject(label.getString());
        }
    }

    public static IASString getString(ResultSet rs, int idx) throws SQLException {
        if (rs instanceof IASResultSet trs) {
            return trs.getTString(idx);
        } else if (rs.isWrapperFor(IASResultSet.class)) {
            IASResultSet iasResultSet = rs.unwrap(IASResultSet.class);
            return iasResultSet.getTString(idx);
        } else {
            String rv = rs.getString(idx);
            return IASString.fromString(rv);
        }
    }

    public static IASString getString(ResultSet rs, IASString label) throws SQLException {
        if (rs instanceof IASResultSet trs) {
            return trs.getTString(label.getString());
        } else if (rs.isWrapperFor(IASResultSet.class)) {
            IASResultSet iasResultSet = rs.unwrap(IASResultSet.class);
            return iasResultSet.getTString(label.getString());
        } else {
            String rv = rs.getString(label.getString());
            return IASString.fromString(rv);
        }
    }
}
