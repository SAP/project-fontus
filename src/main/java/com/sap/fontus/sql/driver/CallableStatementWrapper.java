package com.sap.fontus.sql.driver;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

//TODO is it working tho?
public class CallableStatementWrapper extends PreparedStatementWrapper implements CallableStatement {

    private final CallableStatement delegate;

    public static CallableStatement wrap(CallableStatement delegate) {
        if (delegate == null) {
            return null;
        }
        return new CallableStatementWrapper(delegate);
    }

    protected CallableStatementWrapper(CallableStatement delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public URL getURL(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getURL(parameterIndex);
    }

    @Override
    public void setURL(String parameterName, URL val) throws SQLException {
        SQLException e = null;
        this.delegate.setURL(parameterName, val);
    }

    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException {
        this.delegate.setNull(parameterName, sqlType);
    }

    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        this.delegate.setBoolean(parameterName, x);
    }

    @Override
    public void setByte(String parameterName, byte x) throws SQLException {
        this.delegate.setByte(parameterName, x);
    }

    @Override
    public void setShort(String parameterName, short x) throws SQLException {
        this.delegate.setShort(parameterName, x);
    }

    @Override
    public void setInt(String parameterName, int x) throws SQLException {
        this.delegate.setInt(parameterName, x);
    }

    @Override
    public void setLong(String parameterName, long x) throws SQLException {
        this.delegate.setLong(parameterName, x);
    }

    @Override
    public void setFloat(String parameterName, float x) throws SQLException {
        this.delegate.setFloat(parameterName, x);
    }

    @Override
    public void setDouble(String parameterName, double x) throws SQLException {
        this.delegate.setDouble(parameterName, x);
    }

    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        this.delegate.setBigDecimal(parameterName, x);
    }

    @Override
    public void setString(String parameterName, String x) throws SQLException {
        this.delegate.setString(parameterName, x);
    }

    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        this.delegate.setBytes(parameterName, x);
    }

    @Override
    public void setDate(String parameterName, Date x) throws SQLException {
        this.delegate.setDate(parameterName, x);
    }

    @Override
    public void setTime(String parameterName, Time x) throws SQLException {
        this.delegate.setTime(parameterName, x);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        this.delegate.setTimestamp(parameterName, x);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        this.delegate.setAsciiStream(parameterName, x, length);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        this.delegate.setBinaryStream(parameterName, x, length);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        this.delegate.setObject(parameterName, x, targetSqlType, scale);
    }

    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        this.delegate.setObject(parameterName, x, targetSqlType);
    }

    @Override
    public void setObject(String parameterName, Object x) throws SQLException {
        this.delegate.setObject(parameterName, x);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        this.delegate.setCharacterStream(parameterName, reader, length);
    }

    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        this.delegate.setDate(parameterName, x, cal);
    }

    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        this.delegate.setTime(parameterName, x, cal);
    }

    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        this.delegate.setTimestamp(parameterName, x, cal);
    }

    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        this.delegate.setNull(parameterName, sqlType, typeName);
    }

    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException {
        this.delegate.setRowId(parameterName, x);
    }

    @Override
    public void setNString(String parameterName, String value) throws SQLException {
        this.delegate.setNString(parameterName, value);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        this.delegate.setNCharacterStream(parameterName, value, length);
    }

    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException {
        this.delegate.setNClob(parameterName, value);
    }

    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        this.delegate.setClob(parameterName, reader, length);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        this.delegate.setBlob(parameterName, inputStream, length);
    }

    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        this.delegate.setNClob(parameterName, reader, length);
    }

    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        this.delegate.setSQLXML(parameterName, xmlObject);
    }

    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException {
        this.delegate.setBlob(parameterName, x);
    }

    @Override
    public void setClob(String parameterName, Clob x) throws SQLException {
        this.delegate.setClob(parameterName, x);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        this.delegate.setAsciiStream(parameterName, x, length);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        this.delegate.setBinaryStream(parameterName, x, length);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        this.delegate.setCharacterStream(parameterName, reader, length);
    }

    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        this.delegate.setAsciiStream(parameterName, x);
    }

    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        this.delegate.setBinaryStream(parameterName, x);
    }

    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        this.delegate.setCharacterStream(parameterName, reader);
    }

    @Override
    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        this.delegate.setNCharacterStream(parameterName, value);
    }

    @Override
    public void setClob(String parameterName, Reader reader) throws SQLException {
        this.delegate.setClob(parameterName, reader);
    }

    @Override
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        this.delegate.setBlob(parameterName, inputStream);
    }

    @Override
    public void setNClob(String parameterName, Reader reader) throws SQLException {
        this.delegate.setNClob(parameterName, reader);
    }

    @Override
    public NClob getNClob(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getNClob(parameterIndex);
    }

    @Override
    public NClob getNClob(String parameterName) throws SQLException {
        return this.delegate.getNClob(parameterName);
    }

    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getSQLXML(parameterIndex);
    }

    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return this.delegate.getSQLXML(parameterName);
    }

    @Override
    public String getNString(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getNString(parameterIndex);
    }

    @Override
    public String getNString(String parameterName) throws SQLException {
        return this.delegate.getNString(parameterName);
    }

    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getNCharacterStream(parameterIndex);
    }

    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return this.delegate.getNCharacterStream(parameterName);
    }

    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getCharacterStream(parameterIndex);
    }

    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException {
        return this.delegate.getCharacterStream(parameterName);
    }

    @Override
    public String getString(String parameterName) throws SQLException {
        return this.delegate.getString(parameterName);
    }

    @Override
    public boolean getBoolean(String parameterName) throws SQLException {
        return this.delegate.getBoolean(parameterName);
    }

    @Override
    public byte getByte(String parameterName) throws SQLException {
        return this.delegate.getByte(parameterName);
    }

    @Override
    public short getShort(String parameterName) throws SQLException {
        return this.delegate.getShort(parameterName);
    }

    @Override
    public int getInt(String parameterName) throws SQLException {
        return this.delegate.getInt(parameterName);
    }

    @Override
    public long getLong(String parameterName) throws SQLException {
        return this.delegate.getLong(parameterName);
    }

    @Override
    public float getFloat(String parameterName) throws SQLException {
        return this.delegate.getFloat(parameterName);
    }

    @Override
    public double getDouble(String parameterName) throws SQLException {
        return this.delegate.getDouble(parameterName);
    }

    @Override
    public byte[] getBytes(String parameterName) throws SQLException {
        return this.delegate.getBytes(parameterName);
    }

    @Override
    public Date getDate(String parameterName) throws SQLException {
        return this.delegate.getDate(parameterName);
    }

    @Override
    public Time getTime(String parameterName) throws SQLException {
        return this.delegate.getTime(parameterName);
    }

    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return this.delegate.getTimestamp(parameterName);
    }

    @Override
    public Object getObject(String parameterName) throws SQLException {
        return this.delegate.getObject(parameterName);
    }

    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return this.delegate.getBigDecimal(parameterName);
    }

    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        return this.delegate.getObject(parameterName, map);
    }

    @Override
    public Ref getRef(String parameterName) throws SQLException {
        return this.delegate.getRef(parameterName);
    }

    @Override
    public Blob getBlob(String parameterName) throws SQLException {
        return this.delegate.getBlob(parameterName);
    }

    @Override
    public Clob getClob(String parameterName) throws SQLException {
        return this.delegate.getClob(parameterName);
    }

    @Override
    public Array getArray(String parameterName) throws SQLException {
        return this.delegate.getArray(parameterName);
    }

    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return this.delegate.getDate(parameterName, cal);
    }

    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return this.delegate.getTime(parameterName, cal);
    }

    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        return this.delegate.getTimestamp(parameterName, cal);
    }

    @Override
    public URL getURL(String parameterName) throws SQLException {
        return this.delegate.getURL(parameterName);
    }

    @Override
    public RowId getRowId(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getRowId(parameterIndex);
    }

    @Override
    public RowId getRowId(String parameterName) throws SQLException {
        return this.delegate.getRowId(parameterName);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        this.delegate.registerOutParameter(parameterIndex, sqlType);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        this.delegate.registerOutParameter(parameterIndex, sqlType, scale);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.delegate.wasNull();
    }

    @Override
    public String getString(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getString(parameterIndex);
    }

    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getBoolean(parameterIndex);
    }

    @Override
    public byte getByte(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getByte(parameterIndex);
    }

    @Override
    public short getShort(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getShort(parameterIndex);
    }

    @Override
    public int getInt(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getInt(parameterIndex);
    }

    @Override
    public long getLong(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getLong(parameterIndex);
    }

    @Override
    public float getFloat(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getFloat(parameterIndex);
    }

    @Override
    public double getDouble(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getDouble(parameterIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getBigDecimal(parameterIndex, scale);
    }

    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getBytes(parameterIndex);
    }

    @Override
    public Date getDate(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getDate(parameterIndex);
    }

    @Override
    public Time getTime(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getTime(parameterIndex);
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getTimestamp(parameterIndex);
    }

    @Override
    public Object getObject(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getObject(parameterIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getBigDecimal(parameterIndex);
    }

    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getObject(parameterIndex, map);
    }

    @Override
    public Ref getRef(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getRef(parameterIndex);
    }

    @Override
    public Blob getBlob(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getBlob(parameterIndex);
    }

    @Override
    public Clob getClob(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getClob(parameterIndex);
    }

    @Override
    public Array getArray(int parameterIndex) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getArray(parameterIndex);
    }

    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getDate(parameterIndex, cal);
    }

    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getTime(parameterIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getTimestamp(parameterIndex, cal);
    }

    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        this.delegate.registerOutParameter(parameterIndex, sqlType, typeName);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        this.delegate.registerOutParameter(parameterName, sqlType);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        this.delegate.registerOutParameter(parameterName, sqlType, scale);
    }

    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        this.delegate.registerOutParameter(parameterName, sqlType, typeName);
    }

    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        parameterIndex = (parameterIndex*2)-1;
        return this.delegate.getObject(parameterIndex, type);
    }

    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return this.delegate.getObject(parameterName, type);
    }

}

