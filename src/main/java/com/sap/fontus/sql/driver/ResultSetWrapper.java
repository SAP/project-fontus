package com.sap.fontus.sql.driver;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.taintaware.unified.TaintInformationFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class ResultSetWrapper extends AbstractWrapper implements IASResultSet {
    private final ResultSet delegate;

    public static ResultSet wrap(ResultSet delegate) {
        if (delegate == null) {
            return null;
        }
        return new ResultSetWrapper(delegate);
    }

    public ResultSetWrapper(ResultSet delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public boolean next() throws SQLException {
        return delegate.next();
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public boolean wasNull() throws SQLException {
        return delegate.wasNull();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        String value = delegate.getString(columnIndex);
        return value;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        boolean value = delegate.getBoolean(columnIndex);
        return value;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        byte value = delegate.getByte(columnIndex);
        return value;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        short value = delegate.getShort(columnIndex);
        return value;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        int value = delegate.getInt(columnIndex);
        return value;
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        long value = delegate.getLong(columnIndex);
        return value;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        float value = delegate.getFloat(columnIndex);
        return value;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        double value = delegate.getDouble(columnIndex);
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        BigDecimal value = delegate.getBigDecimal(columnIndex, scale);
        return value;
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        byte[] value = delegate.getBytes(columnIndex);
        return value;
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        Date value = delegate.getDate(columnIndex);
        return value;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        Time value = delegate.getTime(columnIndex);
        return value;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        Timestamp value = delegate.getTimestamp(columnIndex);
        return value;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        InputStream value = delegate.getAsciiStream(columnIndex);
        return value;
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        InputStream value = delegate.getUnicodeStream(columnIndex);
        return value;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        InputStream value = delegate.getBinaryStream(columnIndex);
        return value;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        String value = delegate.getString(columnLabel);
        return value;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        boolean value = delegate.getBoolean(columnLabel);
        return value;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        byte value = delegate.getByte(columnLabel);
        return value;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        short value = delegate.getShort(columnLabel);
        return value;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        int value = delegate.getInt(columnLabel);
        return value;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        long value = delegate.getLong(columnLabel);
        return value;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        float value = delegate.getFloat(columnLabel);
        return value;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        double value = delegate.getDouble(columnLabel);
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        BigDecimal value = delegate.getBigDecimal(columnLabel, scale);
        return value;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        byte[] value = delegate.getBytes(columnLabel);
        return value;
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        Date value = delegate.getDate(columnLabel);
        return value;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        Time value = delegate.getTime(columnLabel);
        return value;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        Timestamp value = delegate.getTimestamp(columnLabel);
        return value;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        InputStream value = delegate.getAsciiStream(columnLabel);
        return value;
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        InputStream value = delegate.getUnicodeStream(columnLabel);
        return value;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        InputStream value = delegate.getBinaryStream(columnLabel);
        return value;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return delegate.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return ResultSetMetaDataWrapper.wrap(delegate.getMetaData());
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        Object value = delegate.getObject(columnIndex);
        return value;
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        Object value = delegate.getObject(columnLabel);
        return value;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return delegate.findColumn(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        Reader value = delegate.getCharacterStream(columnIndex);
        return value;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        Reader value = delegate.getCharacterStream(columnLabel);
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        BigDecimal value = delegate.getBigDecimal(columnIndex);
        return value;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        BigDecimal value = delegate.getBigDecimal(columnLabel);
        return value;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return delegate.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return delegate.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return delegate.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return delegate.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        delegate.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        delegate.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return delegate.first();
    }

    @Override
    public boolean last() throws SQLException {
        return delegate.last();
    }

    @Override
    public int getRow() throws SQLException {
        return delegate.getRow();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return delegate.absolute(row);
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return delegate.relative(rows);
    }

    @Override
    public boolean previous() throws SQLException {
        return delegate.previous();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        //TODO i dont get that tbh
        delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        //TODO i dont get that tbh
        return delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return delegate.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return delegate.getConcurrency();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return delegate.rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return delegate.rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return delegate.rowDeleted();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateNull(columnIndex);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBoolean(columnIndex, x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateByte(columnIndex, x);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateShort(columnIndex, x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateInt(columnIndex, x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateLong(columnIndex, x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateFloat(columnIndex, x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateDouble(columnIndex, x);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBigDecimal(columnIndex, x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateString(columnIndex, x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBytes(columnIndex, x);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateDate(columnIndex, x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateTime(columnIndex, x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateTimestamp(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateObject(columnIndex, x, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateObject(columnIndex, x);
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        delegate.updateNull(columnLabel);
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        delegate.updateBoolean(columnLabel, x);
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        delegate.updateByte(columnLabel, x);
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        delegate.updateShort(columnLabel, x);
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        delegate.updateInt(columnLabel, x);
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        delegate.updateLong(columnLabel, x);
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        delegate.updateFloat(columnLabel, x);
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        delegate.updateDouble(columnLabel, x);
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        delegate.updateBigDecimal(columnLabel, x);
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        delegate.updateString(columnLabel, x);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        delegate.updateBytes(columnLabel, x);
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        delegate.updateDate(columnLabel, x);
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        delegate.updateTime(columnLabel, x);
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        delegate.updateTimestamp(columnLabel, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        delegate.updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        delegate.updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        delegate.updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        delegate.updateObject(columnLabel, x, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        delegate.updateObject(columnLabel, x);
    }

    @Override
    public void insertRow() throws SQLException {
        delegate.insertRow();
    }

    @Override
    public void updateRow() throws SQLException {
        delegate.updateRow();
    }

    @Override
    public void deleteRow() throws SQLException {
        delegate.deleteRow();
    }

    @Override
    public void refreshRow() throws SQLException {
        delegate.refreshRow();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        delegate.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        delegate.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        delegate.moveToCurrentRow();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return delegate.getStatement();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        columnIndex = (columnIndex*2);
        Object value = delegate.getObject(columnIndex, map);
        return value;
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        Ref value = delegate.getRef(columnIndex);
        return value;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        Blob value = delegate.getBlob(columnIndex);
        return value;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        Clob value = delegate.getClob(columnIndex);
        return value;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        Array value = delegate.getArray(columnIndex);
        return value;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        Object value = delegate.getObject(columnLabel, map);
        return value;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        Ref value = delegate.getRef(columnLabel);
        return value;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        Blob value = delegate.getBlob(columnLabel);
        return value;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        Clob value = delegate.getClob(columnLabel);
        return value;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        Array value = delegate.getArray(columnLabel);
        return value;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        columnIndex = (columnIndex*2);
        Date value = delegate.getDate(columnIndex, cal);
        return value;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        Date value = delegate.getDate(columnLabel, cal);
        return value;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        columnIndex = (columnIndex*2);
        Time value = delegate.getTime(columnIndex, cal);
        return value;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        Time value = delegate.getTime(columnLabel, cal);
        return value;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        columnIndex = (columnIndex*2);
        Timestamp value = delegate.getTimestamp(columnIndex, cal);
        return value;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        Timestamp value = delegate.getTimestamp(columnLabel, cal);
        return value;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        URL value = delegate.getURL(columnIndex);
        return value;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        URL value = delegate.getURL(columnLabel);
        return value;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateRef(columnIndex, x);
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        delegate.updateRef(columnLabel, x);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBlob(columnIndex, x);
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        delegate.updateBlob(columnLabel, x);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateClob(columnIndex, x);
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        delegate.updateClob(columnLabel, x);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateArray(columnIndex, x);
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        delegate.updateArray(columnLabel, x);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        RowId value = delegate.getRowId(columnIndex);
        return value;
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        RowId value = delegate.getRowId(columnLabel);
        return value;
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        //TODO: do i have to change the RowId?
        columnIndex = (columnIndex*2);
        delegate.updateRowId(columnIndex, x);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        //TODO: do i have to change the RowId?
        delegate.updateRowId(columnLabel, x);
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateNString(columnIndex, nString);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        delegate.updateNString(columnLabel, nString);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateNClob(columnIndex, nClob);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        delegate.updateNClob(columnLabel, nClob);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        NClob value = delegate.getNClob(columnIndex);
        return value;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        NClob value = delegate.getNClob(columnLabel);
        return value;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        SQLXML value = delegate.getSQLXML(columnIndex);
        return value;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        SQLXML value = delegate.getSQLXML(columnLabel);
        return value;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateSQLXML(columnIndex, xmlObject);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        delegate.updateSQLXML(columnLabel, xmlObject);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        String value = delegate.getNString(columnIndex);
        return value;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        String value = delegate.getNString(columnLabel);
        return value;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2);
        Reader value = delegate.getNCharacterStream(columnIndex);
        return value;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        Reader value = delegate.getNCharacterStream(columnLabel);
        return value;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateNCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateNCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        delegate.updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        delegate.updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBlob(columnIndex, inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        delegate.updateBlob(columnLabel, inputStream, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateClob(columnIndex, reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateClob(columnLabel, reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateNClob(columnIndex, reader, length);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateNClob(columnLabel, reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateNCharacterStream(columnIndex, x);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        delegate.updateNCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateAsciiStream(columnIndex, x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBinaryStream(columnIndex, x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateCharacterStream(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        delegate.updateAsciiStream(columnLabel, x);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        delegate.updateBinaryStream(columnLabel, x);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        delegate.updateCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateBlob(columnIndex, inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        delegate.updateBlob(columnLabel, inputStream);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateClob(columnIndex, reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        delegate.updateClob(columnLabel, reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        columnIndex = (columnIndex*2);
        delegate.updateNClob(columnIndex, reader);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        delegate.updateNClob(columnLabel, reader);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        columnIndex = (columnIndex*2);
        T value = delegate.getObject(columnIndex, type);
        return value;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        T value = delegate.getObject(columnLabel, type);
        return value;
    }

    @Override
    public IASString getTString(int columnIndex) throws SQLException {
        columnIndex = (columnIndex*2)-1;
        return this.getTStringHelper(columnIndex);
    }

    @Override
    public IASString getTString(String columnLabel) throws SQLException {
        int columnIndex = this.getColumnIndex(columnLabel);
        return this.getTStringHelper(columnIndex);
    }

    private IASString getTStringHelper(int idx) throws SQLException {
        String value = this.delegate.getString(idx);
        String taint = this.delegate.getString(idx+1);

        IASString rv = IASString.fromString(value);
        if (taint != null && !taint.equals("0")) {
            System.out.printf("Restoring taint for '%s': %s%n", value, taint);
            Gson gson = new GsonBuilder().serializeNulls().create();
            IASTaintRanges ranges = gson.fromJson(taint, IASTaintRanges.class);
            IASTaintInformationable tis = TaintInformationFactory.createTaintInformation(ranges.getLength(), ranges.getTaintRanges());
            rv.setTaint(tis);
        }
        return rv;
    }

    // TODO: Ugly Hack, requires caching I suppose
    private int getColumnIndex(String name) throws SQLException {
        ResultSetMetaData meta = this.delegate.getMetaData();
        //System.out.println("Looking for column index for: " + name);
        //System.out.println("Column count: " + meta.getColumnCount());
        for(int i = 1; i <= meta.getColumnCount(); i++) {
            String columnName = meta.getColumnName(i);
            //System.out.printf("Column %d:%s [%s]%n", i, columnName, meta.getColumnLabel(i));
            if(columnName.equalsIgnoreCase(name) || meta.getColumnLabel(i).equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }
}

