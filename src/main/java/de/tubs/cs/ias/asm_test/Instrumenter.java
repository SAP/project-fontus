package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

public class Instrumenter {

    public byte[] instrumentClass(InputStream in) throws IOException {
        return this.instrumentInternal(new ClassReader(in));
    }

    public byte[] instrumentClass(byte[] classFileBuffer) {
        return this.instrumentInternal(new ClassReader(classFileBuffer));
    }

    private byte[] instrumentInternal(ClassReader cr) {
        ClassWriter writer = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        //ClassVisitor cca = new CheckClassAdapter(writer);
        ClassTaintingVisitor smr = new ClassTaintingVisitor(writer);
        cr.accept(smr, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

}
