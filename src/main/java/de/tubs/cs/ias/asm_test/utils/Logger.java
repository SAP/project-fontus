package de.tubs.cs.ias.asm_test.utils;

public class Logger extends ParentLogger {
    private final String sourceClass;

    public Logger(String sourceClass) {
        this.setUseParentHandlers(true);
        this.sourceClass = sourceClass;
    }

    @Override
    public String getSourceClassName() {
        return sourceClass;
    }
}
