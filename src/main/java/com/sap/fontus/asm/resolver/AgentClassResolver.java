package com.sap.fontus.asm.resolver;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.Constants;
import com.sap.fontus.utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class AgentClassResolver implements IClassResolver {

    private static final Logger logger = LogUtils.getLogger();

    protected final ClassLoader classLoader;
    protected final LoadingCache<String, byte[]> classCache;

    AgentClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.classCache = Caffeine.newBuilder().build(this::loadClassBytes);
    }

    public void initialize() {

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

    public Optional<byte[]> resolve(String className) {
        // TODO: what about multi-dimensional arrays?
        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }

        byte[] bytes = this.classCache.get(className);
        return Optional.ofNullable(bytes);
    }

}
