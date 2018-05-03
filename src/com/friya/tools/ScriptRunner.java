// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.tools;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptRunner
{
    private static Logger logger;
    private static final String DEFAULT_DELIMITER = ";";
    public static final Pattern delimP;
    private final Connection connection;
    private final boolean stopOnError;
    private final boolean autoCommit;
    private PrintWriter logWriter;
    private PrintWriter errorLogWriter;
    private String delimiter;
    private boolean fullLineDelimiter;
    
    static {
        ScriptRunner.logger = Logger.getLogger(ScriptRunner.class.getName());
        delimP = Pattern.compile("^\\s*(--)?\\s*delimiter\\s*=?\\s*([^\\s]+)+\\s*.*$", 2);
    }
    
    public ScriptRunner(final Connection connection, final boolean autoCommit, final boolean stopOnError) {
        this.logWriter = new PrintWriter(System.out);
        this.errorLogWriter = new PrintWriter(System.err);
        this.delimiter = ";";
        this.fullLineDelimiter = false;
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }
    
    public void setDelimiter(final String delimiter, final boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }
    
    public void setLogWriter(final PrintWriter logWriter) {
        this.logWriter = logWriter;
    }
    
    public void setErrorLogWriter(final PrintWriter errorLogWriter) {
        this.errorLogWriter = errorLogWriter;
    }
    
    public void runScript(final Reader reader) throws IOException, SQLException {
        try {
            final boolean originalAutoCommit = this.connection.getAutoCommit();
            try {
                if (originalAutoCommit != this.autoCommit) {
                    this.connection.setAutoCommit(this.autoCommit);
                }
                this.runScript(this.connection, reader);
            }
            finally {
                this.connection.setAutoCommit(originalAutoCommit);
            }
            this.connection.setAutoCommit(originalAutoCommit);
        }
        catch (IOException | SQLException ex2) {
            final Exception ex;
            final Exception e = ex;
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }
    
    private void runScript(final Connection conn, final Reader reader) throws IOException, SQLException {
        StringBuffer command = null;
        try {
            final LineNumberReader lineReader = new LineNumberReader(reader);
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                line = line.replace("UNSIGNED ", "");
                line = line.replace("SOURCE                  'game-server' NOT NULL", "SOURCE                  VARCHAR(255) DEFAULT 'game-server' NOT NULL");
                line = line.replace("CREATED                   DATE          ", "CREATED                   DATETIME          ");
                line = line.replace("BEGIN TRANSACTION", "BEGIN");
                final String trimmedLine = line.trim();
                final Matcher delimMatch = ScriptRunner.delimP.matcher(trimmedLine);
                if (trimmedLine.length() >= 1 && !trimmedLine.startsWith("//")) {
                    if (delimMatch.matches()) {
                        this.setDelimiter(delimMatch.group(2), false);
                    }
                    else if (trimmedLine.startsWith("--")) {
                        this.println(trimmedLine);
                    }
                    else {
                        if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
                            continue;
                        }
                        if ((!this.fullLineDelimiter && trimmedLine.endsWith(this.getDelimiter())) || (this.fullLineDelimiter && trimmedLine.equals(this.getDelimiter()))) {
                            command.append(line, 0, line.lastIndexOf(this.getDelimiter()));
                            command.append(" ");
                            this.execCommand(conn, command, lineReader);
                            command = null;
                        }
                        else {
                            command.append(line);
                            command.append("\n");
                        }
                    }
                }
            }
            if (command != null) {
                this.execCommand(conn, command, lineReader);
            }
            if (!this.autoCommit) {
                conn.commit();
            }
        }
        catch (Exception e) {
            throw new IOException(String.format("Error executing '%s': %s", command, e.getMessage()), e);
        }
        finally {
            conn.rollback();
            this.flush();
        }
        conn.rollback();
        this.flush();
    }
    
    private void execCommand(final Connection conn, final StringBuffer command, final LineNumberReader lineReader) throws SQLException {
        final Statement statement = conn.createStatement();
        this.println(command);
        boolean hasResults = false;
        try {
            hasResults = statement.execute(command.toString());
        }
        catch (SQLException e) {
            final String errText = String.format("Error executing '%s' (line %d): %s", command, lineReader.getLineNumber(), e.getMessage());
            if (this.stopOnError) {
                throw new SQLException(errText, e);
            }
            this.println(errText);
        }
        if (this.autoCommit && !conn.getAutoCommit()) {
            conn.commit();
        }
        final ResultSet rs = statement.getResultSet();
        if (hasResults && rs != null) {
            final ResultSetMetaData md = rs.getMetaData();
            final int cols = md.getColumnCount();
            for (int i = 1; i <= cols; ++i) {
                final String name = md.getColumnLabel(i);
                this.print(String.valueOf(name) + "\t");
            }
            this.println("");
            while (rs.next()) {
                for (int i = 1; i <= cols; ++i) {
                    final String value = rs.getString(i);
                    this.print(String.valueOf(value) + "\t");
                }
                this.println("");
            }
        }
        try {
            statement.close();
        }
        catch (Exception ex) {}
    }
    
    private String getDelimiter() {
        return this.delimiter;
    }
    
    private void print(final Object o) {
        ScriptRunner.logger.info(new StringBuilder().append(o).toString());
    }
    
    private void println(final Object o) {
        ScriptRunner.logger.info(new StringBuilder().append(o).toString());
    }
    
    private void flush() {
        if (this.logWriter != null) {
            this.logWriter.flush();
        }
        if (this.errorLogWriter != null) {
            this.errorLogWriter.flush();
        }
    }
}
