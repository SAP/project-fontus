package com.sap.fontus.instrumentation;

import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.FontusNonClassLoadingClassWriter;
import com.sap.fontus.asm.TypeHierarchyReaderWithLoaderSupport;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.mutabilitydetector.asm.NonClassloadingClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

public class Instrumenter {
    private static final Logger logger = LogUtils.getLogger();

    public byte[] instrumentClass(InputStream in, ClassResolver resolver, Configuration config, boolean containsJSRRET) throws IOException {
        return instrumentInternal(new ClassReader(in), resolver, config, null, containsJSRRET);
    }

    public byte[] instrumentClass(byte[] classFileBuffer, ClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET) {
        return instrumentInternal(new ClassReader(classFileBuffer), resolver, config, loader, containsJSRRET);
    }

    private static byte[] instrumentInternal(ClassReader cr, ClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET) {
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
