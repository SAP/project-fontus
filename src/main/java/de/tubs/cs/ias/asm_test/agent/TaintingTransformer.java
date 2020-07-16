package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.instrumentation.Instrumenter;
import de.tubs.cs.ias.asm_test.utils.JdkClassesLookupTable;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;

class TaintingTransformer implements ClassFileTransformer {
    private static final ParentLogger logger = LogUtils.getLogger();

    private final Configuration config;
    private final Instrumenter instrumenter;

    TaintingTransformer(Configuration config) {
        this.instrumenter = new Instrumenter();
        this.config = config;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if(loader == null) {
            return classfileBuffer;
        }

        if (JdkClassesLookupTable.getInstance().isJdkClass(className)) {
            logger.info("Skipping JDK class: {}", className);
            return classfileBuffer;
        }
        if (className.startsWith("de/tubs/cs/ias/asm_test")) {
            logger.info("Skipping Tainting Framework class: {}", className);
            return classfileBuffer;
        }

        logger.info("Tainting class: {}", className);
        byte[] outArray = this.instrumenter.instrumentClass(classfileBuffer, new ClassResolver(loader), this.config);
        if (this.config.isVerbose()) {
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
        return outArray;
    }

}
