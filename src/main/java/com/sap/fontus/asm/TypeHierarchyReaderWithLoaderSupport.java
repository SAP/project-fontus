package com.sap.fontus.asm;

import com.sap.fontus.asm.resolver.IClassResolver;
import org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.io.IOException;

public class TypeHierarchyReaderWithLoaderSupport extends TypeHierarchyReader {

    private final IClassResolver resolver;

    public TypeHierarchyReaderWithLoaderSupport(IClassResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected ClassReader reader(Type t) throws IOException {
        return new ClassReaderWithLoaderSupport(this.resolver, t.getInternalName());
    }
}
