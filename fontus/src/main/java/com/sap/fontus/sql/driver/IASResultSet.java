package com.sap.fontus.sql.driver;

import com.sap.fontus.taintaware.unified.IASString;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IASResultSet extends ResultSet {
    Object getTObject(int columnIndex) throws SQLException;
    Object getTObject(String columnLabel) throws SQLException;
    IASString getTString(int columnIndex) throws SQLException;
    IASString getTString(String columnLabel) throws SQLException;
}
