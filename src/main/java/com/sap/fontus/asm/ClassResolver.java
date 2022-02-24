package com.sap.fontus.asm;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.Constants;
import com.sap.fontus.utils.IOUtils;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClassResolver implements IClassResolver {
    private static final Logger logger = LogUtils.getLogger();
    private final List<ClassLoader> loaders = new ArrayList<>();

    private final LoadingCache<String, byte[]> classCache;

    public ClassResolver() {
        this.classCache = Caffeine.newBuilder().build(this::loadClassBytes);
    }

    public void addClassLoader(ClassLoader classLoader) {
        if (!this.loaders.contains(classLoader)) {
            this.loaders.add(classLoader);
        }
    }

    @Override
    public InputStream resolve(String className) {
        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }

        byte[] bytes = this.classCache.get(className);

        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }
        return null;
    }

    private byte[] loadClassBytes(String className) throws IOException {
        String fixed = Utils.dotToSlash(className) + Constants.CLASS_FILE_SUFFIX;
        logger.info("Trying to resolve {} from {}", fixed, className);
        for (ClassLoader loader : this.loaders) {
            InputStream is = loader.getResourceAsStream(fixed);
            if (is != null) {
                return IOUtils.readStream(is);
            }
        }

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fixed);
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(fixed);
        }
        if (is != null) {
            return IOUtils.readStream(is);
        }
        return null;
    }
}
