package com.sap.fontus.asm.resolver;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.Constants;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentClassResolver implements IClassResolver {
    private static final Logger logger = LogUtils.getLogger();
    private final ExecutorService executorService;
    private final ClassLoader classLoader;
    private final LoadingCache<String, byte[]> classCache;

    AgentClassResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.classCache = Caffeine.newBuilder().build(this::loadClassBytes);
        this.executorService = Configuration.getConfiguration().isParallel() ? Executors.newCachedThreadPool() : Executors.newSingleThreadExecutor();
    }

    void initialize() {
        if (classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    if ("jar".equals(url.getProtocol())) {
                        this.executorService.submit(() -> this.loadUrl(url));
                    }
                }
            }
        }

        if (!Configuration.getConfiguration().isParallel()) {
            this.executorService.shutdown();
            try {
                this.executorService.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUrl(URL url) {
        try {
            InputStream inputStream = url.openStream();
            JarClassResolver jarClassResolver = new JarClassResolver();

            Map<String, byte[]> classes = jarClassResolver.loadClassesFrom(inputStream);

            classes.forEach(BytecodeRegistry.getInstance()::addClassData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<byte[]> resolve(String className) {
        if (className.startsWith("[L") && className.endsWith(";")) {
            className = className.substring(2, className.length() - 1);
        }

        Optional<byte[]> commonCached = BytecodeRegistry.getInstance().getClassData(className);
        if (commonCached.isPresent()) {
            return commonCached;
        }

        byte[] bytes = this.classCache.get(className);

        return Optional.ofNullable(bytes);
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
