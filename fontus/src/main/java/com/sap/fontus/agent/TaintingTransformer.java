package com.sap.fontus.agent;

import com.sap.fontus.asm.speculative.SpeculativeParallelInstrumenter;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.Instrumenter;
import com.sap.fontus.instrumentation.InstrumenterInterface;
import com.sap.fontus.utils.*;
import com.sap.fontus.utils.lookups.AnnotationLookup;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import com.sap.fontus.asm.resolver.ClassResolverFactory;
import org.objectweb.asm.ClassReader;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TaintingTransformer implements ClassFileTransformer {
    private static final Logger logger = LogUtils.getLogger();

    private final Configuration config;
    private final InstrumenterInterface instrumenter;
    private final ClassFinder classFinder;

    private final Map<String, byte[]> classCache = new ConcurrentHashMap<>(32000);

    private int nClasses;
    private int nInstrumented;

    TaintingTransformer(Configuration config) {
        this.config = config;
        this.classFinder = ClassResolverFactory.createClassFinder();

        if (config.isSpeculativeActive()) {
            this.instrumenter = SpeculativeParallelInstrumenter.getInstance();
        } else {
            this.instrumenter = new Instrumenter(config);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        this.nClasses += 1;

        if (className == null) {
            className = new ClassReader(classfileBuffer).getClassName();
        }

        BytecodeRegistry.getInstance().addClassData(className, classfileBuffer);
        AnnotationLookup.getInstance().checkAnnotationAndCache(className, classfileBuffer);

        this.classFinder.addClass(className, loader);

        if (loader == null) {
            return classfileBuffer;
        }

        CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(loader);

        if (this.config.isHybridMode() && combinedExcludedLookup.isClassAlreadyInstrumentedForHybrid(className)) {
            if(LogUtils.LOGGING_ENABLED) {
                logger.info("Skipping already instrumented class in hybrid mode: {}", className);
            }
            return classfileBuffer;
        }

        if (combinedExcludedLookup.isJdkClass(className)) {
            if(LogUtils.LOGGING_ENABLED) {
                logger.info("Skipping JDK class: {}", className);
            }
            return classfileBuffer;
        }

        if (combinedExcludedLookup.isExcluded(className)) {
            if(LogUtils.LOGGING_ENABLED) {
                logger.info("Skipping excluded class: {}", className);
            }
            return classfileBuffer;
        }

        if (combinedExcludedLookup.isFontusClass(className)) {
            if(LogUtils.LOGGING_ENABLED) {
                logger.info("Skipping Tainting Framework class: {}", className);
            }
            return classfileBuffer;
        }

        if (combinedExcludedLookup.isProxyClass(className, classfileBuffer)) {
            if(LogUtils.LOGGING_ENABLED) {
                logger.info("Skipping self generated proxy class: {}", className);
            }
            return classfileBuffer;
        }

        try {
            int hash = Arrays.hashCode(classfileBuffer);
            byte[] outArray = null;
            if (this.config.usePersistentCache() && CacheHandler.get().isCached(hash)) {
                if(LogUtils.LOGGING_ENABLED) {
                    logger.info("Fetching class {} from cache", className);
                }
                outArray = CacheHandler.get().fetchFromCache(hash, className);
            } else {
                if(LogUtils.LOGGING_ENABLED) {
                    logger.info("Tainting class: {}", className);
                }
                outArray = this.instrumenter.instrumentClassByteArray(classfileBuffer, loader, className);
                if (this.config.usePersistentCache()) {
                    CacheHandler.get().put(hash, outArray, className);
                }
            }
            this.classCache.put(className, outArray);
            VerboseLogger.saveIfVerbose(className, outArray);
            this.nInstrumented += 1;
            if (this.config.isShowWelcomeMessage() && ((this.nInstrumented % 100) == 0)) {
                System.out.println("FONTUS: Processed " + this.nClasses + " classes, Instrumented " + this.nInstrumented + " classes");
            }
            return outArray;
        } catch (Exception e) {
            System.err.printf("Exception: %s - %s%n", e.getClass().getName(), e.getMessage());
            Configuration.getConfiguration().addExcludedClass(className);
            logger.error("Instrumentation failed for {}. Reason: {}. Class added to excluded classes!", className, e.getMessage());
            Utils.logStackTrace(Arrays.asList(e.getStackTrace()));
        }
        return null;
    }

    public byte[] findInstrumentedClass(String qn) {
        return this.classCache.get(qn);
    }

}
