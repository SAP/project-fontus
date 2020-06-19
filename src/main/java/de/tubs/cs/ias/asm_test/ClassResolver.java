package de.tubs.cs.ias.asm_test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;

public class ClassResolver {
    private final ClassLoader loader;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ClassResolver(ClassLoader loader) {
        this.loader = loader;
    }

    public InputStream resolve(String className) {
        if(className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length()-1);
        }
        String fixed = Utils.fixupReverse(className) + Constants.CLASS_FILE_SUFFIX;
        logger.info("Trying to resolve {} from {}", fixed, className);
        return this.loader.getResourceAsStream(fixed);
    }
}
