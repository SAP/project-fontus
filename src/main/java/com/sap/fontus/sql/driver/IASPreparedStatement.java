package com.sap.fontus.sql.driver;

import com.sap.fontus.taintaware.unified.IASString;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IASPreparedStatement extends PreparedStatement {
    void setString(int parameterIndex, IASString x) throws SQLException;

}
