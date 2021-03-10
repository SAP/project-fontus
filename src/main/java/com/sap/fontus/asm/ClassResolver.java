package com.sap.fontus.asm;

import com.sap.fontus.Constants;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;

import java.io.InputStream;

public class ClassResolver {
    private final ClassLoader loader;
    private static final Logger logger = LogUtils.getLogger();

    public ClassResolver(ClassLoader loader) {
        this.loader = loader;
    }

    public InputStream resolve(String className) {
        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }
        String fixed = Utils.dotToSlash(className) + Constants.CLASS_FILE_SUFFIX;
        logger.info("Trying to resolve {} from {}", fixed, className);
        InputStream is = this.loader.getResourceAsStream(fixed);
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fixed);
            if (is == null) {
                is = ClassLoader.getSystemResourceAsStream(fixed);
            }
        }
        return is;
    }
}
