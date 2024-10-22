package com.sap.fontus.config.taintloss;

import com.sap.fontus.taintaware.IASTaintAware;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileLoggingTaintlossHandler extends LoggingTaintlossHandler {
    @Override
    protected void handleTaintlossInternal(IASTaintAware taintAware, List<StackTraceElement> stackTrace) {
        String log = this.format(taintAware, stackTrace);
        File logFile = new File("taintloss.log");
        try {
            Files.write(logFile.toPath(), log.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "file_logging";
    }

    static {
        TaintlossHandler.add(new FileLoggingTaintlossHandler());
    }
}
