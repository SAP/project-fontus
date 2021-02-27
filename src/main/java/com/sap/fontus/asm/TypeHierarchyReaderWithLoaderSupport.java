package com.sap.fontus.asm;

import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.io.IOException;

public class TypeHierarchyReaderWithLoaderSupport extends TypeHierarchyReader {

    private final ClassResolver resolver;

    public TypeHierarchyReaderWithLoaderSupport(ClassResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected ClassReader reader(Type t) throws IOException {
        return new ClassReaderWithLoaderSupport(this.resolver, t.getInternalName());
    }
}
