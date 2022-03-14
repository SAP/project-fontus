package com.sap.fontus.agent;

import java.io.File;

public class InstrumentationConfiguration {
    private static InstrumentationConfiguration instance;
    private final File input;
    private final File output;

    public InstrumentationConfiguration(File input, File output) {
        this.input = input;
        this.output = output;
    }

    public static synchronized void init(File input, File output) {
        instance = new InstrumentationConfiguration(input, output);
    }

    public static InstrumentationConfiguration getInstance() {
        if (instance == null) {
            throw new RuntimeException("Instrumentation info not initialized!");
        }
        return instance;
    }

    public File getInput() {
        return input;
    }

    public File getOutput() {
        return output;
    }
}
