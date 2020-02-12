package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.ClassResolver;
import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Instrumenter;
import de.tubs.cs.ias.asm_test.JdkClassesLookupTable;
import de.tubs.cs.ias.asm_test.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;

class TaintingTransformer implements ClassFileTransformer {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Configuration config;
    private final Instrumenter instrumenter;
    private static final JdkClassesLookupTable jdkClasses = JdkClassesLookupTable.instance;

    TaintingTransformer(Configuration config) {
        this.instrumenter = new Instrumenter();
        this.config = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (isJdkClass(className)) {
            logger.info("Skipping JDK class: {}", className);
            return classfileBuffer;
        }
        if (className.startsWith("de/tubs/cs/ias/asm_test")) {
            logger.info("Skipping Tainting Framework class: {}", className);
            return classfileBuffer;
        }

        logger.info("Tainting class: {}", className);
        byte[] outArray = this.instrumenter.instrumentClass(classfileBuffer, new ClassResolver(loader), config);
        if (this.config.isVerbose()) {
            String baseName = "/tmp/agent";
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
        return outArray;
    }

    static boolean isJdkClass(String className) {
        return jdkClasses.isJdkClass(className);
    }
}
