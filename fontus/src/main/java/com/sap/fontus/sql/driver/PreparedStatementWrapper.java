package com.sap.fontus.sql.driver;

import com.sap.fontus.sql.tainter.QueryParameters;
import com.sap.fontus.sql.tainter.TaintAssignment;
import com.sap.fontus.taintaware.unified.IASString;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

public class PreparedStatementWrapper extends StatementWrapper implements IASPreparedStatement {

    private final PreparedStatement delegate;
    private final QueryParameters parameters;
    private String originalQuery="";
    private String taintedQuery="";

    public static PreparedStatement wrap(PreparedStatement delegate,String originalQuery,String taintedQuery, QueryParameters parameters) {
        if (delegate == null) {
            return null;
        }
        return new PreparedStatementWrapper(delegate,originalQuery,taintedQuery, parameters);
    }
    protected PreparedStatementWrapper(PreparedStatement delegate, QueryParameters parameters){
        super(delegate);
        this.delegate=delegate;
        this.parameters = parameters;
    }

    protected PreparedStatementWrapper(PreparedStatement delegate, String originalQuery,String taintedQuery, QueryParameters parameters) {
        super(delegate);
        this.parameters = parameters;
        this.taintedQuery=taintedQuery;
        this.originalQuery=originalQuery;
        this.delegate = delegate;
    }
    public QueryParameters getParameters() {
        return this.parameters;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return ResultSetWrapper.wrap(this.delegate.executeQuery());
    }

    @Override
    public int executeUpdate() throws SQLException {
        return this.delegate.executeUpdate();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        // System.out.printf("Set at idx %d to value null%n", parameterIndex);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNull(assignment.getNewIndex(), sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        // System.out.printf("Set at idx %d to value %b%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBoolean(assignment.getNewIndex(), x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        // System.out.printf("Set at idx %d to value %d%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setByte(assignment.getNewIndex(), x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        // System.out.printf("Set at idx %d to value %d%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setShort(assignment.getNewIndex(), x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        // System.out.printf("Set at idx %d to value %d%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setInt(assignment.getNewIndex(), x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        //System.out.printf("Set at idx %d to value %d%nQuery: %s (%s)", parameterIndex, x, this.taintedQuery, this.originalQuery);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setLong(assignment.getNewIndex(), x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        // System.out.printf("Set at idx %d to value %f%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setFloat(assignment.getNewIndex(), x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        // System.out.printf("Set at idx %d to value %f%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setDouble(assignment.getNewIndex(), x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBigDecimal(assignment.getNewIndex(), x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        //System.out.printf("SetString at idx %d to value %s%n", parameterIndex, x);
        //Utils.printCurrentStackTrace();
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setString(assignment.getNewIndex(), x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, Arrays.toString(x));
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBytes(assignment.getNewIndex(), x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setDate(assignment.getNewIndex(), x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setTime(assignment.getNewIndex(), x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setTimestamp(assignment.getNewIndex(), x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setAsciiStream(assignment.getNewIndex(), x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        //noinspection deprecation
        this.delegate.setUnicodeStream(assignment.getNewIndex(), x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBinaryStream(assignment.getNewIndex(), x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        this.delegate.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setObject(assignment.getNewIndex(), x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setObject(assignment.getNewIndex(), x);
    }

    @Override
    public boolean execute() throws SQLException {
        return this.delegate.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        this.delegate.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setCharacterStream(assignment.getNewIndex(), reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setRef(assignment.getNewIndex(), x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBlob(assignment.getNewIndex(), x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setClob(assignment.getNewIndex(), x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setArray(assignment.getNewIndex(), x);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setDate(assignment.getNewIndex(), x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setTime(assignment.getNewIndex(), x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setTimestamp(assignment.getNewIndex(), x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, "null");
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNull(assignment.getNewIndex(), sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setURL(assignment.getNewIndex(), x);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setRowId(assignment.getNewIndex(), x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, value);
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNString(assignment.getNewIndex(), value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNCharacterStream(assignment.getNewIndex(), value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNClob(assignment.getNewIndex(), value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setClob(assignment.getNewIndex(), reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBlob(assignment.getNewIndex(), inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNClob(assignment.getNewIndex(), reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setSQLXML(assignment.getNewIndex(), xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setObject(assignment.getNewIndex(), x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setAsciiStream(assignment.getNewIndex(), x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBinaryStream(assignment.getNewIndex(), x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setCharacterStream(assignment.getNewIndex(), reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setAsciiStream(assignment.getNewIndex(), x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBinaryStream(assignment.getNewIndex(), x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setCharacterStream(assignment.getNewIndex(), reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNCharacterStream(assignment.getNewIndex(), value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setClob(assignment.getNewIndex(), reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setBlob(assignment.getNewIndex(), inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
        this.delegate.setNClob(assignment.getNewIndex(), reader);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return this.delegate.getParameterMetaData();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return ResultSetMetaDataWrapper.wrap(this.delegate.getMetaData());
    }

    @Override
    public void setString(int parameterIndex, IASString x) throws SQLException {
        //System.out.println(Arrays.toString(this.setVariables));
        //System.out.println(Arrays.toString(this.newIndex));
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);

        this.delegate.setString(assignment.getNewIndex(), x != null ? x.getString() : null);
        //System.out.printf("Setting tainted? (%b) string in prep statement: %s%n", x.isTainted(), x.getString());
        if(x != null && x.isTainted() && assignment.isHasTaint()) {
            //System.out.printf("Setting String at idx %d: %s/%s%n", parameterIndex, x.getString(), "foo");
            //System.out.println(Arrays.toString(this.setVariables));
            //System.out.println(Arrays.toString(this.newIndex));
            String json = Utils.serializeTaints(x);
            this.delegate.setString(assignment.getTaintIndex(), json);
        }else if( assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
    }

    @Override
    public void setNString(int parameterIndex, IASString value) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        this.delegate.setNString(assignment.getNewIndex(), value != null ? value.getString() : null);
        if(value != null &&value.isTainted() && assignment.isHasTaint()) {
            //System.out.printf("Setting String at idx %d: %s/%s%n", parameterIndex, x.getString(), "foo");
            //System.out.println(Arrays.toString(this.setVariables));
            //System.out.println(Arrays.toString(this.newIndex));
            String json = Utils.serializeTaints(value);
            this.delegate.setString(assignment.getTaintIndex(), json);
        }else if( assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
    }

    private void setTaint(IASString value, int parameterIndex) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(value.isTainted() && assignment.isHasTaint()) {
            //System.out.printf("Setting String at idx %d: %s/%s%n", parameterIndex, x.getString(), "foo");
            //System.out.println(Arrays.toString(this.setVariables));
            //System.out.println(Arrays.toString(this.newIndex));
            String json = Utils.serializeTaints(value);
            this.delegate.setString(assignment.getTaintIndex(), json);
        } else if( assignment.isHasTaint()) {
            this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
        }
    }
    @Override
    public void setTObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if(x instanceof IASString && (
                targetSqlType == Types.VARCHAR ||
                targetSqlType == Types.NVARCHAR ||
                targetSqlType == Types.LONGNVARCHAR ||
                targetSqlType == Types.LONGVARCHAR)) {
            IASString value = (IASString) x;
            this.delegate.setObject(assignment.getNewIndex(), value.getString(), targetSqlType);
            if (assignment.isHasTaint()) {
                this.delegate.setString(assignment.getTaintIndex(), value.isTainted() ? Utils.serializeTaints(value) : Constants.UNTAINTED);
            }
        } else {
            this.delegate.setObject(assignment.getNewIndex(), x, targetSqlType);
            if (assignment.isHasTaint()) {
                this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
            }
        }
    }

    @Override
    public void setTObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if (x instanceof IASString && (
                targetSqlType == Types.VARCHAR ||
                targetSqlType == Types.NVARCHAR ||
                targetSqlType == Types.LONGNVARCHAR ||
                targetSqlType == Types.LONGVARCHAR)) {
            IASString value = (IASString) x;
            this.delegate.setObject(assignment.getNewIndex(), value.getString(), targetSqlType, scaleOrLength);
            if (assignment.isHasTaint()) {
                this.delegate.setString(assignment.getTaintIndex(), value.isTainted() ? Utils.serializeTaints(value) : Constants.UNTAINTED);
            }
        } else {
            this.delegate.setObject(assignment.getNewIndex(), x, targetSqlType, scaleOrLength);
            if (assignment.isHasTaint()) {
                this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
            }
        }
    }

    @Override
    public void setTObject(int parameterIndex, Object x) throws SQLException {
        TaintAssignment assignment = this.parameters.computeAssignment(parameterIndex);
        if (x instanceof IASString) {
            IASString value = (IASString) x;
            this.delegate.setObject(assignment.getNewIndex(), value.getString());
            if (assignment.isHasTaint()) {
               this.delegate.setString(assignment.getTaintIndex(), value.isTainted() ? Utils.serializeTaints(value) : Constants.UNTAINTED);
            }
        } else {
            this.delegate.setObject(assignment.getNewIndex(), x);
            if (assignment.isHasTaint()) {
                this.delegate.setString(assignment.getTaintIndex(), Constants.UNTAINTED);
            }
        }

    }
}

