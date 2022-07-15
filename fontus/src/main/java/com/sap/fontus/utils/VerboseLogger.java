package com.sap.fontus.utils;

import com.sap.fontus.Constants;
import com.sap.fontus.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VerboseLogger {
    private static final Logger logger = LogUtils.getLogger();

    private VerboseLogger() {
    }

    public static void saveIfVerbose(String className, byte[] outArray) {
        if (Configuration.getConfiguration().isVerbose()) {
            save(className, outArray);
        }
    }

    public static void save(String className, byte[] outArray) {
        String baseName = "./tmp/agent";
        File outFile = new File(baseName, className + Constants.CLASS_FILE_SUFFIX);
        File parent = new File(outFile.getParent());
        parent.mkdirs();
        try {
            outFile.createNewFile();
            Path p = outFile.toPath();
            Files.write(p, outArray);
        } catch (IOException e) {
            logger.error("Failed to write class file", e);
        }
    }
}
