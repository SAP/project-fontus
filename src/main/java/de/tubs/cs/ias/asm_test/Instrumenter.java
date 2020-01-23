package de.tubs.cs.ias.asm_test;

import org.mutabilitydetector.asm.NonClassloadingClassWriter;
import de.tubs.cs.ias.asm_test.config.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

public class Instrumenter {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public byte[] instrumentClass(InputStream in, Configuration config) throws IOException {
        return instrumentInternal(new ClassReader(in), config);
    }

    public byte[] instrumentClass(byte[] classFileBuffer, Configuration config) {
        return instrumentInternal(new ClassReader(classFileBuffer), config);
    }

    private static byte[] instrumentInternal(ClassReader cr, Configuration config) {
        NonClassloadingClassWriter writer = new NonClassloadingClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        ClassTaintingVisitor smr = new ClassTaintingVisitor(writer, config);
        cr.accept(smr, ClassReader.SKIP_FRAMES);
        String clazzName = cr.getClassName();
        String superName = cr.getSuperName();
        String[] interfaces = cr.getInterfaces();
        String ifs = String.join(", ", interfaces);
        logger.info("{} <- {} implements: {}", clazzName, superName, interfaces);
        return writer.toByteArray();
    }

}
