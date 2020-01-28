package de.tubs.cs.ias.asm_test;

import java.io.InputStream;

public class ClassResolver {
    private final ClassLoader loader;

    public ClassResolver(ClassLoader loader) {
        this.loader = loader;
    }

    public InputStream resolve(String className) {
        return this.loader.getResourceAsStream(Utils.fixupReverse(className) + Constants.CLASS_FILE_SUFFIX);
    }
}
