package com.sap.fontus.sql.driver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.unified.IASString;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreparedStatementWrapper extends StatementWrapper implements IASPreparedStatement {

    private final PreparedStatement delegate;

    //change this line if you want to use another pattern
    private String originalQuery="";
    private String taintedQuery="";
    private final String findingPattern ="([_\\.a-zA-Z0-9]*)\\s*([<>=]\\s*\\?)";
    private final String findAllPattern = "(([_\\.a-zA-Z0-9]*)|(__[_\\.a-zA-Z0-9]*__))\\s*([<>=]\\s*\\?)";
    private String [] originalIndex;
    private int [] newIndex;
    private boolean [] setVariables;

    public static PreparedStatement wrap(PreparedStatement delegate,String originalQuery,String taintedQuery) {
        long countOriginal = originalQuery.chars().filter(q -> q == '?').count();
        long countTainted = taintedQuery.chars().filter(q -> q == '?').count();
        if (delegate == null) {
            return null;
        }
        return new PreparedStatementWrapper(delegate,countOriginal,countTainted,originalQuery,taintedQuery);
    }
    protected PreparedStatementWrapper(PreparedStatement delegate){
        super(delegate);
        this.delegate=delegate;
    }

    protected PreparedStatementWrapper(PreparedStatement delegate,long countOriginal, long countTainted,String originalQuery,String taintedQuery) {
        super(delegate);
        this.taintedQuery=taintedQuery;
        this.originalQuery=originalQuery;
        this.setVariables=new boolean[(int)countTainted];
        this.originalIndex=new String [(int)countOriginal];
        this.newIndex=new int[(int)countOriginal];
        this.delegate = delegate;
        Pattern searchPattern = Pattern.compile(findingPattern);
        Matcher searchMatcher = searchPattern.matcher(originalQuery);
        int count=0;
        while(searchMatcher.find()){
            originalIndex[count] = searchMatcher.group().replaceAll("\\s+","");
            count++;
        }
        int indexCount = 0;
        if(count > 0) {

            String original = originalIndex[indexCount];
            Pattern indexShufflePattern = Pattern.compile(findAllPattern);
            Matcher indexShuffleMatcher = indexShufflePattern.matcher(taintedQuery);
            count = 1;
            while (indexShuffleMatcher.find()) {
                String group = indexShuffleMatcher.group().replaceAll("\\s+", "");
                if (original.equals(group)) {
                    newIndex[indexCount] = count;
                    if (indexCount < originalIndex.length - 1) {
                        indexCount++;
                    }
                    original = originalIndex[indexCount];
                }
                count++;
            }

        }
        if (indexCount == 0) {
            int idx = 1;
            for (int i = 0; i < countOriginal; i++) {
                newIndex[i] = idx;
                idx += 2;
            }
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        for(int i=0;i<setVariables.length;i++){
            if(setVariables[i]!=true){
                delegate.setNull(i+1, Types.VARCHAR);
            }
        }
        return ResultSetWrapper.wrap(delegate.executeQuery());
    }

    @Override
    public int executeUpdate() throws SQLException {
        int rowCount = 0;
        for(int i=0;i<setVariables.length;i++){
            if(setVariables[i]!=true){
                delegate.setNull(i+1, Types.VARCHAR);
            }
        }
        rowCount = delegate.executeUpdate();
        return rowCount;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        // System.out.printf("Set at idx %d to value null%n", parameterIndex);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNull(newIndex[parameterIndex-1], sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        // System.out.printf("Set at idx %d to value %b%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBoolean(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        // System.out.printf("Set at idx %d to value %d%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setByte(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        // System.out.printf("Set at idx %d to value %d%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setShort(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        // System.out.printf("Set at idx %d to value %d%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setInt(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        // System.out.printf("Set at idx %d to value %d%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setLong(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        // System.out.printf("Set at idx %d to value %f%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setFloat(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        // System.out.printf("Set at idx %d to value %f%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setDouble(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBigDecimal(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setString(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, Arrays.toString(x));
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBytes(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setDate(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setTime(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setTimestamp(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setAsciiStream(newIndex[parameterIndex-1], x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setUnicodeStream(newIndex[parameterIndex-1], x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBinaryStream(newIndex[parameterIndex-1], x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        delegate.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setObject(newIndex[parameterIndex-1], x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setObject(newIndex[parameterIndex-1], x);
    }

    @Override
    public boolean execute() throws SQLException {
        for(int i=0;i<setVariables.length;i++){
            if(setVariables[i]!=true){
                delegate.setNull(i+1, Types.VARCHAR);
            }
        }
        return delegate.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        for(int i=0;i<setVariables.length;i++){
            if(setVariables[i]!=true){
                delegate.setNull(i+1, Types.VARCHAR);
            }
        }
        delegate.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setCharacterStream(newIndex[parameterIndex-1], reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setRef(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBlob(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setClob(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setArray(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setDate(newIndex[parameterIndex-1], x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setTime(newIndex[parameterIndex-1], x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setTimestamp(newIndex[parameterIndex-1], x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, "null");
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNull(newIndex[parameterIndex-1], sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, x);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setURL(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setRowId(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        // System.out.printf("Set at idx %d to value %s%n", parameterIndex, value);
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNString(newIndex[parameterIndex-1], value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNCharacterStream(newIndex[parameterIndex-1], value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNClob(newIndex[parameterIndex-1], value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setClob(newIndex[parameterIndex-1], reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBlob(newIndex[parameterIndex-1], inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNClob(newIndex[parameterIndex-1], reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setSQLXML(newIndex[parameterIndex-1], xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setObject(newIndex[parameterIndex-1], x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setAsciiStream(newIndex[parameterIndex-1], x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBinaryStream(newIndex[parameterIndex-1], x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setCharacterStream(newIndex[parameterIndex-1], reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setAsciiStream(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBinaryStream(newIndex[parameterIndex-1], x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setCharacterStream(newIndex[parameterIndex-1], reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNCharacterStream(newIndex[parameterIndex-1], value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setClob(newIndex[parameterIndex-1], reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setBlob(newIndex[parameterIndex-1], inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setNClob(newIndex[parameterIndex-1], reader);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return delegate.getParameterMetaData();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return ResultSetMetaDataWrapper.wrap(this.delegate.getMetaData());
    }

    @Override
    public void setString(int parameterIndex, IASString x) throws SQLException {
        //System.out.println(Arrays.toString(this.setVariables));
        //System.out.println(Arrays.toString(this.newIndex));
        this.setVariables[newIndex[parameterIndex-1]-1]=true;
        delegate.setString(newIndex[parameterIndex-1], x.getString());
        if(x.isTainted()) {
            this.setVariables[newIndex[parameterIndex - 1]] = true;
            //System.out.printf("Setting String at idx %d: %s/%s%n", parameterIndex, x.getString(), "foo");
            //System.out.println(Arrays.toString(this.setVariables));
            //System.out.println(Arrays.toString(this.newIndex));
            Gson gson = new GsonBuilder().serializeNulls().create();
            String json = gson.toJson(x.getTaintInformationInitialized().getTaintRanges(x.length()));
            //to restore:
            // IASTaintRanges ranges = gson.fromJson(json, IASTaintRanges.class);
            delegate.setString(newIndex[parameterIndex-1]+1, json);
        }
    }
}

