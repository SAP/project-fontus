package de.tubs.cs.ias.asm_test.utils;

public class Logger extends ParentLogger {
    private final String sourceClass;

    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers set to true.
     *
     * @param name A name for the logger.  This should
     *             be a dot-separated name and should normally
     *             be based on the package name or class name
     *             of the subsystem, such as java.net
     *             or javax.swing.  It may be null for anonymous Loggers.
     */
    public Logger(String sourceClass) {
        this.setUseParentHandlers(true);
        this.sourceClass = sourceClass;
    }

    @Override
    public String getSourceClassName() {
        return sourceClass;
    }
}
