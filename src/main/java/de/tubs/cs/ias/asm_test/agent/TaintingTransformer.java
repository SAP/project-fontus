package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.instrumentation.Instrumenter;
import de.tubs.cs.ias.asm_test.utils.JdkClassesLookupTable;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;

class TaintingTransformer implements ClassFileTransformer {
    private static final ParentLogger logger = LogUtils.getLogger();

    private final Configuration config;
    private final Instrumenter instrumenter;
    private final Instrumentation instrumentation;

    TaintingTransformer(Configuration config, Instrumentation instrumentation) {
        this.instrumenter = new Instrumenter();
        this.config = config;
        this.instrumentation = instrumentation;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (loader == null) {
            return classfileBuffer;
        }

        if (className == null) {
            className = new ClassReader(classfileBuffer).getClassName();
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
        try {
            byte[] outArray = this.instrumenter.instrumentClass(classfileBuffer, new ClassResolver(loader), this.config, loader);
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
        } catch (Exception e) {
            logger.error("Instrumentation failed for {}. Reason: {}", className, e.getMessage());
//            System.err.println("Instrumentation failed for " + className);
//            e.printStackTrace();
        }
        return null;
    }

}
