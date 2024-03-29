package com.sap.fontus.sql.driver;


import com.sap.fontus.taintaware.unified.IASString;

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

    private static int transformColumnIndex(int columnIndex) {
        return (columnIndex * 2) - 1;
    }

    @Override
    public boolean next() throws SQLException {
        return this.delegate.next();
    }

    @Override
    public void close() throws SQLException {
        this.delegate.close();
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.delegate.wasNull();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return this.delegate.getString(transformColumnIndex(columnIndex));
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return this.delegate.getBoolean(transformColumnIndex(columnIndex));
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return this.delegate.getByte(transformColumnIndex(columnIndex));
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return this.delegate.getShort(transformColumnIndex(columnIndex));
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return this.delegate.getInt(transformColumnIndex(columnIndex));
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return this.delegate.getLong(transformColumnIndex(columnIndex));
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return this.delegate.getFloat(transformColumnIndex(columnIndex));
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return this.delegate.getDouble(transformColumnIndex(columnIndex));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return this.delegate.getBigDecimal(transformColumnIndex(columnIndex), scale);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return this.delegate.getBytes(transformColumnIndex(columnIndex));
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return this.delegate.getDate(transformColumnIndex(columnIndex));
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return this.delegate.getTime(transformColumnIndex(columnIndex));
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return this.delegate.getTimestamp(transformColumnIndex(columnIndex));
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return this.delegate.getAsciiStream(transformColumnIndex(columnIndex));
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return this.delegate.getUnicodeStream(transformColumnIndex(columnIndex));
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return this.delegate.getBinaryStream(transformColumnIndex(columnIndex));
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return this.delegate.getString(columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return this.delegate.getBoolean(columnLabel);
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return this.delegate.getByte(columnLabel);
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return this.delegate.getShort(columnLabel);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return this.delegate.getInt(columnLabel);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return this.delegate.getLong(columnLabel);
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return this.delegate.getFloat(columnLabel);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return this.delegate.getDouble(columnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return this.delegate.getBigDecimal(columnLabel, scale);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return this.delegate.getBytes(columnLabel);
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return this.delegate.getDate(columnLabel);
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return this.delegate.getTime(columnLabel);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return this.delegate.getTimestamp(columnLabel);
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return this.delegate.getAsciiStream(columnLabel);
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return this.delegate.getUnicodeStream(columnLabel);
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return this.delegate.getBinaryStream(columnLabel);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.delegate.clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return this.delegate.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return ResultSetMetaDataWrapper.wrap(this.delegate.getMetaData());
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return this.delegate.getObject(transformColumnIndex(columnIndex));
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return this.delegate.getObject(columnLabel);
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return this.delegate.findColumn(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return this.delegate.getCharacterStream(transformColumnIndex(columnIndex));
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return this.delegate.getCharacterStream(columnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return this.delegate.getBigDecimal(transformColumnIndex(columnIndex));
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return this.delegate.getBigDecimal(columnLabel);
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.delegate.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return this.delegate.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return this.delegate.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return this.delegate.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        this.delegate.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        this.delegate.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return this.delegate.first();
    }

    @Override
    public boolean last() throws SQLException {
        return this.delegate.last();
    }

    @Override
    public int getRow() throws SQLException {
        return this.delegate.getRow();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return this.delegate.absolute(row);
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return this.delegate.relative(rows);
    }

    @Override
    public boolean previous() throws SQLException {
        return this.delegate.previous();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        //TODO i dont get that tbh
        this.delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        //TODO i dont get that tbh
        return this.delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return this.delegate.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return this.delegate.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return this.delegate.getConcurrency();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return this.delegate.rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return this.delegate.rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return this.delegate.rowDeleted();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        this.delegate.updateNull(transformColumnIndex(columnIndex));
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        this.delegate.updateBoolean(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        this.delegate.updateByte(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        this.delegate.updateShort(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        this.delegate.updateInt(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        this.delegate.updateLong(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        this.delegate.updateFloat(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        this.delegate.updateDouble(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        this.delegate.updateBigDecimal(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        this.delegate.updateString(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        this.delegate.updateBytes(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        this.delegate.updateDate(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        this.delegate.updateTime(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        this.delegate.updateTimestamp(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.delegate.updateAsciiStream(transformColumnIndex(columnIndex), x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.delegate.updateBinaryStream(transformColumnIndex(columnIndex), x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        this.delegate.updateCharacterStream(transformColumnIndex(columnIndex), x, length);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        this.delegate.updateObject(transformColumnIndex(columnIndex), x, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        this.delegate.updateObject(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        this.delegate.updateNull(columnLabel);
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        this.delegate.updateBoolean(columnLabel, x);
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        this.delegate.updateByte(columnLabel, x);
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        this.delegate.updateShort(columnLabel, x);
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        this.delegate.updateInt(columnLabel, x);
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        this.delegate.updateLong(columnLabel, x);
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        this.delegate.updateFloat(columnLabel, x);
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        this.delegate.updateDouble(columnLabel, x);
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        this.delegate.updateBigDecimal(columnLabel, x);
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        this.delegate.updateString(columnLabel, x);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        this.delegate.updateBytes(columnLabel, x);
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        this.delegate.updateDate(columnLabel, x);
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        this.delegate.updateTime(columnLabel, x);
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        this.delegate.updateTimestamp(columnLabel, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        this.delegate.updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        this.delegate.updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        this.delegate.updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        this.delegate.updateObject(columnLabel, x, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        this.delegate.updateObject(columnLabel, x);
    }

    @Override
    public void insertRow() throws SQLException {
        this.delegate.insertRow();
    }

    @Override
    public void updateRow() throws SQLException {
        this.delegate.updateRow();
    }

    @Override
    public void deleteRow() throws SQLException {
        this.delegate.deleteRow();
    }

    @Override
    public void refreshRow() throws SQLException {
        this.delegate.refreshRow();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        this.delegate.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        this.delegate.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        this.delegate.moveToCurrentRow();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return this.delegate.getStatement();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return this.delegate.getObject(transformColumnIndex(columnIndex), map);
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return this.delegate.getRef(transformColumnIndex(columnIndex));
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return this.delegate.getBlob(transformColumnIndex(columnIndex));
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return this.delegate.getClob(transformColumnIndex(columnIndex));
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return this.delegate.getArray(transformColumnIndex(columnIndex));
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return this.delegate.getObject(columnLabel, map);
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return this.delegate.getRef(columnLabel);
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return this.delegate.getBlob(columnLabel);
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return this.delegate.getClob(columnLabel);
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return this.delegate.getArray(columnLabel);
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return this.delegate.getDate(transformColumnIndex(columnIndex), cal);
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return this.delegate.getDate(columnLabel, cal);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return this.delegate.getTime(transformColumnIndex(columnIndex), cal);
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return this.delegate.getTime(columnLabel, cal);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return this.delegate.getTimestamp(transformColumnIndex(columnIndex), cal);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return this.delegate.getTimestamp(columnLabel, cal);
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return this.delegate.getURL(transformColumnIndex(columnIndex));
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return this.delegate.getURL(columnLabel);
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        this.delegate.updateRef(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        this.delegate.updateRef(columnLabel, x);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        this.delegate.updateBlob(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        this.delegate.updateBlob(columnLabel, x);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        this.delegate.updateClob(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        this.delegate.updateClob(columnLabel, x);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        this.delegate.updateArray(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        this.delegate.updateArray(columnLabel, x);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return this.delegate.getRowId(transformColumnIndex(columnIndex));
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return this.delegate.getRowId(columnLabel);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        //TODO: do i have to change the RowId?
        this.delegate.updateRowId(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        //TODO: do i have to change the RowId?
        this.delegate.updateRowId(columnLabel, x);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.delegate.getHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.delegate.isClosed();
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        this.delegate.updateNString(transformColumnIndex(columnIndex), nString);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        this.delegate.updateNString(columnLabel, nString);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        this.delegate.updateNClob(transformColumnIndex(columnIndex), nClob);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        this.delegate.updateNClob(columnLabel, nClob);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return this.delegate.getNClob(transformColumnIndex(columnIndex));
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return this.delegate.getNClob(columnLabel);
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return this.delegate.getSQLXML(transformColumnIndex(columnIndex));
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return this.delegate.getSQLXML(columnLabel);
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        this.delegate.updateSQLXML(transformColumnIndex(columnIndex), xmlObject);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        this.delegate.updateSQLXML(columnLabel, xmlObject);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return this.delegate.getNString(transformColumnIndex(columnIndex));
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return this.delegate.getNString(columnLabel);
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return this.delegate.getNCharacterStream(transformColumnIndex(columnIndex));
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return this.delegate.getNCharacterStream(columnLabel);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        this.delegate.updateNCharacterStream(transformColumnIndex(columnIndex), x, length);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        this.delegate.updateNCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        this.delegate.updateAsciiStream(transformColumnIndex(columnIndex), x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        this.delegate.updateBinaryStream(transformColumnIndex(columnIndex), x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        this.delegate.updateCharacterStream(transformColumnIndex(columnIndex), x, length);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        this.delegate.updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        this.delegate.updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        this.delegate.updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        this.delegate.updateBlob(transformColumnIndex(columnIndex), inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        this.delegate.updateBlob(columnLabel, inputStream, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        this.delegate.updateClob(transformColumnIndex(columnIndex), reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.delegate.updateClob(columnLabel, reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        this.delegate.updateNClob(transformColumnIndex(columnIndex), reader, length);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.delegate.updateNClob(columnLabel, reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        this.delegate.updateNCharacterStream(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.delegate.updateNCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        this.delegate.updateAsciiStream(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        this.delegate.updateBinaryStream(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        this.delegate.updateCharacterStream(transformColumnIndex(columnIndex), x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        this.delegate.updateAsciiStream(columnLabel, x);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        this.delegate.updateBinaryStream(columnLabel, x);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.delegate.updateCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        this.delegate.updateBlob(transformColumnIndex(columnIndex), inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        this.delegate.updateBlob(columnLabel, inputStream);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        this.delegate.updateClob(transformColumnIndex(columnIndex), reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        this.delegate.updateClob(columnLabel, reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        this.delegate.updateNClob(transformColumnIndex(columnIndex), reader);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        this.delegate.updateNClob(columnLabel, reader);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return this.delegate.getObject(transformColumnIndex(columnIndex), type);
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return this.delegate.getObject(columnLabel, type);
    }

    @Override
    public IASString getTString(int columnIndex) throws SQLException {
        return this.getTStringHelper(transformColumnIndex(columnIndex));
    }

    @Override
    public IASString getTString(String columnLabel) throws SQLException {
        int columnIndex = this.delegate.findColumn(columnLabel);
        return this.getTStringHelper(columnIndex);
    }

    private IASString getTStringHelper(int idx) throws SQLException {
        String value = this.delegate.getString(idx);
        String taint = this.delegate.getString(idx + 1);

        IASString rv = IASString.fromString(value);
        if (taint != null && !Constants.UNTAINTED.equals(taint) && !"0".equals(taint)) {
            //System.out.printf("Restoring taint for '%s': %s%n", value, taint);
            Utils.restoreTaint(rv, taint);
        }
        return rv;
    }

    @Override
    public Object getTObject(String columnLabel) throws SQLException {
        return getTObject(this.delegate.findColumn(columnLabel));
    }

    @Override
    public Object getTObject(int columnIndex) throws SQLException {
        int idx = transformColumnIndex(columnIndex);
        Object o = this.delegate.getObject(idx);

        if (o instanceof String) {
            String s = (String) o;
            String taint = this.delegate.getString(idx + 1);
            IASString rv = IASString.fromString(s);
            if (taint != null && !Constants.UNTAINTED.equals(taint) && !"0".equals(taint)) {
                //System.out.printf("Restoring taint for '%s': %s%n", value, taint);
                Utils.restoreTaint(rv, taint);
            }
            o = rv;
        }
        return o;
    }
}

