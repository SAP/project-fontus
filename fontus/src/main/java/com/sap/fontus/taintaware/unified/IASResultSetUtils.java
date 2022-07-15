package com.sap.fontus.taintaware.unified;

import com.sap.fontus.sql.driver.IASResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class IASResultSetUtils {

    private IASResultSetUtils() {
    }

    public static IASString getString(ResultSet rs, int idx) throws SQLException {
        if (rs instanceof IASResultSet) {
            return ((IASResultSet) rs).getTString(idx);
        } else if (rs.isWrapperFor(IASResultSet.class)) {
            IASResultSet iasResultSet = rs.unwrap(IASResultSet.class);
            return iasResultSet.getTString(idx);
        } else {
            String rv = rs.getString(idx);
            return IASString.fromString(rv);
        }
    }

    public static IASString getString(ResultSet rs, IASString label) throws SQLException {
        if (rs instanceof IASResultSet) {
            return ((IASResultSet) rs).getTString(label.getString());
        } else if (rs.isWrapperFor(IASResultSet.class)) {
            IASResultSet iasResultSet = rs.unwrap(IASResultSet.class);
            return iasResultSet.getTString(label.getString());
        } else {
            String rv = rs.getString(label.getString());
            return IASString.fromString(rv);
        }
    }
}
