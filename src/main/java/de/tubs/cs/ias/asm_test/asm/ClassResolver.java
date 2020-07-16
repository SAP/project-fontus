package de.tubs.cs.ias.asm_test.asm;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.Utils;

import java.io.InputStream;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;

public class ClassResolver {
    private final ClassLoader loader;
    private static final ParentLogger logger = LogUtils.getLogger();

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
