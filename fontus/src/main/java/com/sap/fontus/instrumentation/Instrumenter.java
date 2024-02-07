package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.FontusNonClassLoadingClassWriter;
import com.sap.fontus.asm.resolver.ClassResolverFactory;
import com.sap.fontus.asm.resolver.IClassResolver;
import com.sap.fontus.asm.TypeHierarchyReaderWithLoaderSupport;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Instrumenter implements InstrumenterInterface {
    private static final Logger logger = LogUtils.getLogger();

    private final Configuration config;

    public Instrumenter(Configuration config) {
        this.config = config;
    }

    public byte[] instrumentClass(InputStream in, IClassResolver resolver, Configuration config, boolean containsJSRRET) throws IOException {
        return instrumentInternal(new ClassReader(in), resolver, config, null, containsJSRRET);
    }

    public byte[] instrumentClass(byte[] classFileBuffer, IClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET) {
        return instrumentInternal(new ClassReader(classFileBuffer), resolver, config, loader, containsJSRRET);
    }

    public byte[] instrumentClassByteArray(byte[] classfileBuffer, ClassLoader loader, String className) {
        byte[] outArray;
        try {
            outArray = this.instrumentClass(classfileBuffer, ClassResolverFactory.createClassResolver(loader), Configuration.getConfiguration(), loader, false);
        } catch (IllegalArgumentException ex) {
            if ("JSR/RET are not supported with computeFrames option".equals(ex.getMessage())) {
                logger.error("JSR/RET not supported in {}!", className);
                Utils.logStackTrace(Arrays.asList(ex.getStackTrace()));
                outArray = this.instrumentClass(classfileBuffer, ClassResolverFactory.createClassResolver(loader), Configuration.getConfiguration(), loader, true);
                logger.error("Finished retrying {}", className);
            } else {
                logger.error("Instrumentation failed for {}", className);
                Utils.logStackTrace(Arrays.asList(ex.getStackTrace()));
                throw ex;
            }
        }
        return outArray;
    }

    private static byte[] instrumentInternal(ClassReader cr, IClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET) {
        CombinedExcludedLookup excludedLookup = new CombinedExcludedLookup(loader);
        FontusNonClassLoadingClassWriter writer = new FontusNonClassLoadingClassWriter(cr, ClassWriter.COMPUTE_FRAMES, new TypeHierarchyReaderWithLoaderSupport(resolver), excludedLookup);
        ClassTaintingVisitor smr = new ClassTaintingVisitor(writer, resolver, config, loader, containsJSRRET, excludedLookup);
        cr.accept(smr, ClassReader.SKIP_FRAMES);
        String clazzName = cr.getClassName();
        String superName = cr.getSuperName();
        String[] interfaces = cr.getInterfaces();
        String ifs = String.join(", ", interfaces);
        logger.info("{} <- {} implements: {}", clazzName, superName, interfaces);
        return writer.toByteArray();
    }

}
