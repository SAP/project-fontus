package de.tubs.cs.ias.asm_test;

import org.mutabilitydetector.asm.NonClassloadingClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

public class Instrumenter {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public byte[] instrumentClass(InputStream in, ClassLoader loader) throws IOException {
        return instrumentInternal(new ClassReader(in), loader);
    }

    public byte[] instrumentClass(byte[] classFileBuffer, ClassLoader loader) {
        return instrumentInternal(new ClassReader(classFileBuffer), loader);
    }

    private static byte[] instrumentInternal(ClassReader cr, ClassLoader loader) {
        NonClassloadingClassWriter writer = new NonClassloadingClassWriter(cr, ClassWriter.COMPUTE_FRAMES, new TypeHierarchyReaderWithLoaderSupport(loader));

        ClassTaintingVisitor smr = new ClassTaintingVisitor(writer);
        cr.accept(smr, ClassReader.SKIP_FRAMES);
            String clazzName = cr.getClassName();
            String superName = cr.getSuperName();
            String[] interfaces = cr.getInterfaces();
            String ifs = String.join(", ", interfaces);
            logger.info("{} <- {} implements: {}", clazzName, superName, interfaces);
            return writer.toByteArray();

    }

}
