package com.sap.fontus.asm.resolver;

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
import java.util.Map;

public class AgentClassResolver implements IClassResolver {
    private static final Logger logger = LogUtils.getLogger();
    private final ClassLoader classLoader;
    private final Map<String, byte[]> commonCache;

    private final LoadingCache<String, byte[]> classCache;

    AgentClassResolver(ClassLoader classLoader, Map<String, byte[]> commonCache) {
        this.classLoader = classLoader;
        this.commonCache = commonCache;
        this.classCache = Caffeine.newBuilder().build(this::loadClassBytes);
    }

    @Override
    public InputStream resolve(String className) {
        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }

        if (commonCache.containsKey(className)) {
            return new ByteArrayInputStream(commonCache.get(className));
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

        if (this.classLoader != null) {
            InputStream is = this.classLoader.getResourceAsStream(fixed);
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
