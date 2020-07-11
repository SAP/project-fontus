package de.tubs.cs.ias.asm_test.utils;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Logger extends java.util.logging.Logger {
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
    public Logger(String name) {
        super(name, null);
        this.setUseParentHandlers(false);
    }

    public void error(String message, Object... insertions) {
        message = this.format(message, insertions);
        this.log(Level.SEVERE, message);
    }

    public void info(String message, Object... insertions) {
        message = this.format(message, insertions);
        this.info(message);
    }

    private String format(String message, Object... insertions) {
        for(int i = 0; message.contains("{}"); i++) {
            message = message.replaceFirst(Pattern.quote("{}"), "{"+ i +"}");
        }
        return MessageFormat.format(message, insertions);
    }

    public void debug(String message, String insertions) {
        message = this.format(message, insertions);
        this.log(Level.FINE, message);
    }

    public void warn(String message, Object... insertions) {
        message = this.format(message, insertions);
        this.log(Level.WARNING, message);
    }
}
