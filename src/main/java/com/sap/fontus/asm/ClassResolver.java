package com.sap.fontus.asm;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.Constants;
import com.sap.fontus.utils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClassResolver implements IClassResolver {
    private static final Logger logger = LogUtils.getLogger();
    private final ClassLoader classLoader;
    private static final Map<String, byte[]> commonCache = new ConcurrentHashMap<>();

    private final LoadingCache<String, byte[]> classCache;

    public ClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.classCache = Caffeine.newBuilder().build(this::loadClassBytes);
        this.initialize();
    }

    private synchronized void initialize() {
        if (this.classLoader != null && this.classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) this.classLoader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    if ("jar".equals(url.getProtocol())) {
                        new Thread(() -> {
                            try {
                                InputStream inputStream = url.openStream();
                                JarClassResolver jarClassResolver = InstrumentationFactory.createJarClassResolver(inputStream);

                                jarClassResolver.getClasses().forEach(commonCache::putIfAbsent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
            }
        }
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

    public static void addClassData(String internalName, byte[] data) {
        Objects.requireNonNull(internalName);
        Objects.requireNonNull(data);

        internalName = Utils.dotToSlash(internalName);

        commonCache.putIfAbsent(internalName, data);
    }
}
