package com.sap.fontus.agent;

import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.utils.VerboseLogger;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.Instrumenter;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.ClassReader;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

class TaintingTransformer implements ClassFileTransformer {
    private static final Logger logger = LogUtils.getLogger();

    private final Configuration config;
    private final Instrumenter instrumenter;

    private final Map<String, byte[]> classCache = new HashMap<>();

    TaintingTransformer(Configuration config) {
        this.instrumenter = new Instrumenter();
        this.config = config;
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

        CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(loader);
        if (combinedExcludedLookup.isJdkClass(className)) {
            logger.info("Skipping JDK class: {}", className);
            return classfileBuffer;
        }

        if (combinedExcludedLookup.isPackageExcluded(className)) {
            logger.info("Skipping excluded class: {}", className);
            return classfileBuffer;
        }

        if (combinedExcludedLookup.isFontusClass(className)) {
            logger.info("Skipping Tainting Framework class: {}", className);
            return classfileBuffer;
        }

        if (combinedExcludedLookup.isProxyClass(className, classfileBuffer)) {
            logger.info("Skipping self generated proxy class: {}", className);
            return classfileBuffer;
        }

        logger.info("Tainting class: {}", className);
        try {
            byte[] outArray = instrumentClassByteArray(classfileBuffer, loader);
            this.classCache.put(className, outArray);
            VerboseLogger.saveIfVerbose(className, outArray);
            return outArray;
        } catch (Exception e) {
            logger.error("Instrumentation failed for {}. Reason: {}", className, e.getMessage());
        }
        return null;
    }

    public byte[] findInstrumentedClass(String qn) {
        return this.classCache.get(qn);
    }

    private byte[] instrumentClassByteArray(byte[] classfileBuffer, ClassLoader loader) {
        byte[] outArray;
        try {
            outArray = this.instrumenter.instrumentClass(classfileBuffer, new ClassResolver(loader), this.config, loader, false);
        } catch (IllegalArgumentException ex) {
            if ("JSR/RET are not supported with computeFrames option".equals(ex.getMessage())) {
                outArray = this.instrumenter.instrumentClass(classfileBuffer, new ClassResolver(loader), this.config, loader, true);
            } else {
                throw ex;
            }
        }
        return outArray;
    }

}
