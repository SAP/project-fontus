package de.tubs.cs.ias.asm_test;

import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.io.IOException;

public class TypeHierarchyReaderWithLoaderSupport extends TypeHierarchyReader {

    private final ClassLoader loader;

    public TypeHierarchyReaderWithLoaderSupport(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    protected ClassReader reader(Type t) throws IOException {
        return new ClassReaderWithLoaderSupport(this.loader, t.getInternalName());
    }
}
