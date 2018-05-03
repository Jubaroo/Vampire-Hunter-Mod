// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.tools;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

public class LoggableStatement implements PreparedStatement
{
    private ArrayList parameterValues;
    private String sqlTemplate;
    private PreparedStatement wrappedStatement;
    
    public LoggableStatement(final Connection connection, final String sql) throws SQLException {
        this.wrappedStatement = connection.prepareStatement(sql);
        this.sqlTemplate = sql;
        this.parameterValues = new ArrayList();
    }
    
    @Override
    public void addBatch() throws SQLException {
        this.wrappedStatement.addBatch();
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        this.wrappedStatement.addBatch(sql);
    }
    
    @Override
    public void cancel() throws SQLException {
        this.wrappedStatement.cancel();
    }
    
    @Override
    public void clearBatch() throws SQLException {
        this.wrappedStatement.clearBatch();
    }
    
    @Override
    public void clearParameters() throws SQLException {
        this.wrappedStatement.clearParameters();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        this.wrappedStatement.clearWarnings();
    }
    
    @Override
    public void close() throws SQLException {
        this.wrappedStatement.close();
    }
    
    @Override
    public boolean execute() throws SQLException {
        return this.wrappedStatement.execute();
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        return this.wrappedStatement.execute(sql);
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        return this.wrappedStatement.executeBatch();
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        return this.wrappedStatement.executeQuery();
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        return this.wrappedStatement.executeQuery(sql);
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        return this.wrappedStatement.executeUpdate();
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        return this.wrappedStatement.executeUpdate(sql);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return this.wrappedStatement.getConnection();
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return this.wrappedStatement.getFetchDirection();
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return this.wrappedStatement.getFetchSize();
    }
    
    @Override
    public int getMaxFieldSize() throws SQLException {
        return this.wrappedStatement.getMaxFieldSize();
    }
    
    @Override
    public int getMaxRows() throws SQLException {
        return this.wrappedStatement.getMaxRows();
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.wrappedStatement.getMetaData();
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        return this.wrappedStatement.getMoreResults();
    }
    
    @Override
    public int getQueryTimeout() throws SQLException {
        return this.wrappedStatement.getQueryTimeout();
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return this.wrappedStatement.getResultSet();
    }
    
    @Override
    public int getResultSetConcurrency() throws SQLException {
        return this.wrappedStatement.getResultSetConcurrency();
    }
    
    @Override
    public int getResultSetType() throws SQLException {
        return this.wrappedStatement.getResultSetType();
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        return this.wrappedStatement.getUpdateCount();
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.wrappedStatement.getWarnings();
    }
    
    @Override
    public void setArray(final int i, final Array x) throws SQLException {
        this.wrappedStatement.setArray(i, x);
        this.saveQueryParamValue(i, x);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        this.wrappedStatement.setAsciiStream(parameterIndex, x, length);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        this.wrappedStatement.setBigDecimal(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        this.wrappedStatement.setBinaryStream(parameterIndex, x, length);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setBlob(final int i, final Blob x) throws SQLException {
        this.wrappedStatement.setBlob(i, x);
        this.saveQueryParamValue(i, x);
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        this.wrappedStatement.setBoolean(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, new Boolean(x));
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        this.wrappedStatement.setByte(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, new Integer(x));
    }
    
    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        this.wrappedStatement.setBytes(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        this.wrappedStatement.setCharacterStream(parameterIndex, reader, length);
        this.saveQueryParamValue(parameterIndex, reader);
    }
    
    @Override
    public void setClob(final int i, final Clob x) throws SQLException {
        this.wrappedStatement.setClob(i, x);
        this.saveQueryParamValue(i, x);
    }
    
    @Override
    public void setCursorName(final String name) throws SQLException {
        this.wrappedStatement.setCursorName(name);
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        this.wrappedStatement.setDate(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        this.wrappedStatement.setDate(parameterIndex, x, cal);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        this.wrappedStatement.setDouble(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, new Double(x));
    }
    
    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        this.wrappedStatement.setEscapeProcessing(enable);
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        this.wrappedStatement.setFetchDirection(direction);
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        this.wrappedStatement.setFetchSize(rows);
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        this.wrappedStatement.setFloat(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, new Float(x));
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        this.wrappedStatement.setInt(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, new Integer(x));
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        this.wrappedStatement.setLong(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, new Long(x));
    }
    
    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        this.wrappedStatement.setMaxFieldSize(max);
    }
    
    @Override
    public void setMaxRows(final int max) throws SQLException {
        this.wrappedStatement.setMaxRows(max);
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        this.wrappedStatement.setNull(parameterIndex, sqlType);
        this.saveQueryParamValue(parameterIndex, null);
    }
    
    @Override
    public void setNull(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
        this.wrappedStatement.setNull(paramIndex, sqlType, typeName);
        this.saveQueryParamValue(paramIndex, null);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        this.wrappedStatement.setObject(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        this.wrappedStatement.setObject(parameterIndex, x, targetSqlType);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scale) throws SQLException {
        this.wrappedStatement.setObject(parameterIndex, x, targetSqlType, scale);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        this.wrappedStatement.setQueryTimeout(seconds);
    }
    
    @Override
    public void setRef(final int i, final Ref x) throws SQLException {
        this.wrappedStatement.setRef(i, x);
        this.saveQueryParamValue(i, x);
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        this.wrappedStatement.setShort(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, new Integer(x));
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        this.wrappedStatement.setString(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        this.wrappedStatement.setTime(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        this.wrappedStatement.setTime(parameterIndex, x, cal);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        this.wrappedStatement.setTimestamp(parameterIndex, x);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        this.wrappedStatement.setTimestamp(parameterIndex, x, cal);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        this.wrappedStatement.setUnicodeStream(parameterIndex, x, length);
        this.saveQueryParamValue(parameterIndex, x);
    }
    
    public String getQueryString() {
        final StringBuffer buf = new StringBuffer();
        int qMarkCount = 0;
        final StringTokenizer tok = new StringTokenizer(String.valueOf(this.sqlTemplate) + " ", "?");
        while (tok.hasMoreTokens()) {
            final String oneChunk = tok.nextToken();
            buf.append(oneChunk);
            try {
                Object value;
                if (this.parameterValues.size() > 1 + qMarkCount) {
                    value = this.parameterValues.get(1 + qMarkCount++);
                }
                else if (tok.hasMoreTokens()) {
                    value = null;
                }
                else {
                    value = "";
                }
                buf.append(new StringBuilder().append(value).toString());
            }
            catch (Throwable e) {
                buf.append("ERROR WHEN PRODUCING QUERY STRING FOR LOG." + e.toString());
            }
        }
        return buf.toString().trim();
    }
    
    private void saveQueryParamValue(final int position, final Object obj) {
        String strValue;
        if (obj instanceof String || obj instanceof java.util.Date) {
            strValue = "'" + obj + "'";
        }
        else if (obj == null) {
            strValue = "null";
        }
        else {
            strValue = obj.toString();
        }
        while (position >= this.parameterValues.size()) {
            this.parameterValues.add(null);
        }
        this.parameterValues.set(position, strValue);
    }
    
    @Override
    public void closeOnCompletion() {
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) {
        return false;
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) {
        return false;
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) {
        return false;
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) {
        return 0;
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) {
        return 0;
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) {
        return 0;
    }
    
    @Override
    public ResultSet getGeneratedKeys() {
        return null;
    }
    
    @Override
    public boolean getMoreResults(final int current) {
        return false;
    }
    
    @Override
    public int getResultSetHoldability() {
        return 0;
    }
    
    @Override
    public boolean isCloseOnCompletion() {
        return false;
    }
    
    @Override
    public boolean isClosed() {
        return false;
    }
    
    @Override
    public boolean isPoolable() {
        return false;
    }
    
    @Override
    public void setPoolable(final boolean poolable) {
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() {
        return null;
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) {
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) {
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) {
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) {
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) {
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) {
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader) {
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) {
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value) {
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) {
    }
    
    @Override
    public void setNClob(final int parameterIndex, final NClob value) {
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader) {
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length) {
    }
    
    @Override
    public void setNString(final int parameterIndex, final String value) {
    }
    
    @Override
    public void setRowId(final int parameterIndex, final RowId x) {
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) {
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL x) {
    }
}
