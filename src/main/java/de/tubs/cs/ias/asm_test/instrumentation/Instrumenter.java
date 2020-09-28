package de.tubs.cs.ias.asm_test.instrumentation;

import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.asm.TypeHierarchyReaderWithLoaderSupport;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import org.mutabilitydetector.asm.NonClassloadingClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

public class Instrumenter {
    private static final ParentLogger logger = LogUtils.getLogger();

    public byte[] instrumentClass(InputStream in, ClassResolver resolver, Configuration config, boolean containsJSRRET) throws IOException {
        return instrumentInternal(new ClassReader(in), resolver, config, null, containsJSRRET);
    }

    public byte[] instrumentClass(byte[] classFileBuffer, ClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET) {
        return instrumentInternal(new ClassReader(classFileBuffer), resolver, config, loader, containsJSRRET);
    }

    private static byte[] instrumentInternal(ClassReader cr, ClassResolver resolver, Configuration config, ClassLoader loader, boolean containsJSRRET) {
        NonClassloadingClassWriter writer = new NonClassloadingClassWriter(cr, ClassWriter.COMPUTE_FRAMES, new TypeHierarchyReaderWithLoaderSupport(resolver));
        ClassTaintingVisitor smr = new ClassTaintingVisitor(writer, resolver, config, loader, containsJSRRET);
        cr.accept(smr, ClassReader.SKIP_FRAMES);
        String clazzName = cr.getClassName();
        String superName = cr.getSuperName();
        String[] interfaces = cr.getInterfaces();
        String ifs = String.join(", ", interfaces);
        logger.info("{} <- {} implements: {}", clazzName, superName, interfaces);
        return writer.toByteArray();

    }

}
