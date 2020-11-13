package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VerboseLogger {
    private static final ParentLogger logger = LogUtils.getLogger();

    public static void saveIfVerbose(String className, byte[] outArray) {
        if (Configuration.getConfiguration().isVerbose()) {
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
}
