package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class LoaderAwareClassWriter extends ClassWriter {
    private final ClassLoader loader;
    public LoaderAwareClassWriter(ClassLoader loader, ClassReader classReader, int flags) {
        super(classReader, flags);
        this.loader = loader;
    }


    @Override
    protected ClassLoader getClassLoader() {
        return this.loader;
    }
}
