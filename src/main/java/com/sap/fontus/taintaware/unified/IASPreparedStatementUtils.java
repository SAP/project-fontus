package com.sap.fontus.taintaware.unified;

import com.sap.fontus.sql.driver.IASPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IASPreparedStatementUtils {

    public static void setString(PreparedStatement ps, int idx, IASString value) throws SQLException {
        if(ps instanceof IASPreparedStatement) {
            ((IASPreparedStatement) ps).setString(idx, value);
        } else {
            ps.setString(idx, value.getString());
        }
    }
}
