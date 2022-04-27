package com.sap.fontus.utils;

import com.sap.fontus.config.Configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

public class Logger {
    private final String sourceClass;
    private final String outputFile;

    public Logger(String sourceClass, String outputFile) {
        this.sourceClass = sourceClass;
        this.outputFile = outputFile;
    }

    public String getSourceClassName() {
        return "unknown";
    }

    public void log(String level, String message) {
        if (Configuration.isLoggingEnabled()) {
            String line = String.format("%s\t|%s\t|%s", level, this.sourceClass, message);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFile, true));
                writer.write(line);
                writer.newLine();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void error(String message, Object... insertions) {
        if (Configuration.isLoggingEnabled()) {
            message = this.format(message, insertions);
            this.log("ERROR", message);
        }
    }

    public void info(String message, Object... insertions) {
        if (Configuration.isLoggingEnabled()) {
            message = this.format(message, insertions);
            this.log("INFO", message);
        }
    }

    private String format(String message, Object... insertions) {
        for (int i = 0; message.contains("{}"); i++) {
            message = message.replaceFirst(Pattern.quote("{}"), "{" + i + "}");
        }
        return MessageFormat.format(message, insertions);
    }

    public void debug(String message, String insertions) {
        if (Configuration.isLoggingEnabled()) {
            message = this.format(message, insertions);
            this.log("DEBUG", message);
        }
    }

    public void warn(String message, Object... insertions) {
        if (Configuration.isLoggingEnabled()) {
            message = this.format(message, insertions);
            this.log("WARN", message);
        }
    }
}
