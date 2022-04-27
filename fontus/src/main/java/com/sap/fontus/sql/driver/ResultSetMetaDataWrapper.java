package com.sap.fontus.sql.driver;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ResultSetMetaDataWrapper extends AbstractWrapper implements ResultSetMetaData {
    private final ResultSetMetaData delegate;

    public ResultSetMetaDataWrapper(ResultSetMetaData delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public static ResultSetMetaDataWrapper wrap(ResultSetMetaData delegate) {
        if (delegate == null) {
            return null;
        }
        return new ResultSetMetaDataWrapper(delegate);
    }


    @Override
    public int getColumnCount() throws SQLException {
        // System.out.println("getColumnCount(): " + this.delegate.getColumnCount());
        return this.delegate.getColumnCount()/2;
    }

    @Override
    public boolean isAutoIncrement(int i) throws SQLException {
        return this.delegate.isAutoIncrement((i*2)-1);
    }

    @Override
    public boolean isCaseSensitive(int i) throws SQLException {
        return this.delegate.isCaseSensitive((i*2)-1);
    }

    @Override
    public boolean isSearchable(int i) throws SQLException {
        return this.delegate.isSearchable((i*2)-1);
    }

    @Override
    public boolean isCurrency(int i) throws SQLException {
        return this.delegate.isCurrency((i*2)-1);
    }

    @Override
    public int isNullable(int i) throws SQLException {
        return this.delegate.isNullable((i*2)-1);
    }

    @Override
    public boolean isSigned(int i) throws SQLException {
        return this.delegate.isSigned((i*2)-1);
    }

    @Override
    public int getColumnDisplaySize(int i) throws SQLException {
        return this.delegate.getColumnDisplaySize((i*2)-1);
    }

    @Override
    public String getColumnLabel(int i) throws SQLException {
        return this.delegate.getColumnLabel((i*2)-1);
    }

    @Override
    public String getColumnName(int i) throws SQLException {
        return this.delegate.getColumnName((i*2)-1);
    }

    @Override
    public String getSchemaName(int i) throws SQLException {
        return this.delegate.getSchemaName((i*2)-1);
    }

    @Override
    public int getPrecision(int i) throws SQLException {
        return this.delegate.getPrecision(i*2);
    }

    @Override
    public int getScale(int i) throws SQLException {
        return this.delegate.getScale((i*2)-1);
    }

    @Override
    public String getTableName(int i) throws SQLException {
        return this.delegate.getTableName((i*2)-1);
    }

    @Override
    public String getCatalogName(int i) throws SQLException {
        return this.delegate.getCatalogName((i*2)-1);
    }

    @Override
    public int getColumnType(int i) throws SQLException {
        return this.delegate.getColumnType((i*2)-1);
    }

    @Override
    public String getColumnTypeName(int i) throws SQLException {
        return this.delegate.getColumnTypeName((i*2)-1);
    }

    @Override
    public boolean isReadOnly(int i) throws SQLException {
        return this.delegate.isReadOnly((i*2)-1);
    }

    @Override
    public boolean isWritable(int i) throws SQLException {
        return this.delegate.isWritable((i*2)-1);
    }

    @Override
    public boolean isDefinitelyWritable(int i) throws SQLException {
        return this.delegate.isDefinitelyWritable((i*2)-1);
    }

    @Override
    public String getColumnClassName(int i) throws SQLException {
        return this.delegate.getColumnClassName((i*2)-1);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return this.delegate.unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return this.delegate.isWrapperFor(aClass);
    }
}
