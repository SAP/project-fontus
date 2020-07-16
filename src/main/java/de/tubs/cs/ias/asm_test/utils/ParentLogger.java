package de.tubs.cs.ias.asm_test.utils;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class ParentLogger extends java.util.logging.Logger {
    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers set to true.
     *
     * @param name               A name for the logger.  This should
     *                           be a dot-separated name and should normally
     *                           be based on the package name or class name
     *                           of the subsystem, such as java.net
     *                           or javax.swing.  It may be null for anonymous Loggers.
     */
    public ParentLogger() {
        super("InstrumentationLogger", null);
        this.setUseParentHandlers(false);
    }

    public String getSourceClassName() {
        return "unknown";
    }

    public void error(String message, Object... insertions) {
        message = this.format(message, insertions);
        LogRecord logRecord = new LogRecord(Level.SEVERE, message);
        logRecord.setSourceClassName(getSourceClassName());
        this.log(logRecord);
    }

    public void info(String message, Object... insertions) {
        message = this.format(message, insertions);
        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setSourceClassName(getSourceClassName());
        this.log(logRecord);
    }

    private String format(String message, Object... insertions) {
        for(int i = 0; message.contains("{}"); i++) {
            message = message.replaceFirst(Pattern.quote("{}"), "{"+ i +"}");
        }
        return MessageFormat.format(message, insertions);
    }

    public void debug(String message, String insertions) {
        message = this.format(message, insertions);
        LogRecord logRecord = new LogRecord(Level.FINE, message);
        logRecord.setSourceClassName(getSourceClassName());
        this.log(logRecord);
    }

    public void warn(String message, Object... insertions) {
        message = this.format(message, insertions);
        LogRecord logRecord = new LogRecord(Level.WARNING, message);
        logRecord.setSourceClassName(getSourceClassName());
        this.log(logRecord);
    }
}
