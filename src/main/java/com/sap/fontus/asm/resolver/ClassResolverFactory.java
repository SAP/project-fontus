package com.sap.fontus.asm.resolver;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.ClassFinder;
import com.sap.fontus.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ClassResolverFactory {
    private static final Map<String, byte[]> commonClassesCache;
    private static final LoadingCache<ClassLoader, AgentClassResolver> classResolvers;
    private static final OfflineClassResolver offlineClassResolver;
    private static final AgentClassResolver nullResolver;
    private static final ClassFinder classFinder;

    static {
        commonClassesCache = new ConcurrentHashMap<>();
        classResolvers = Caffeine.newBuilder().build(ClassResolverFactory::createOnlineClassResolver);
        nullResolver = new AgentClassResolver(null, commonClassesCache);
        offlineClassResolver = new OfflineClassResolver(classResolvers.get(ClassLoader.getSystemClassLoader()));
        classFinder = new ClassFinder(TaintAgent.getInstrumentation());
    }

    public static IClassResolver createClassResolver(ClassLoader classLoader) {
        if (Configuration.getConfiguration().isOfflineInstrumentation()) {
            return offlineClassResolver;
        } else {
            if (classLoader == null) {
                return nullResolver;
            }
            return classResolvers.get(classLoader);
        }
    }

    public static ClassFinder createClassFinder() {
        return classFinder;
    }

    public static JarClassResolver createJarClassResolver(File jarFile) {
        try (InputStream is = new FileInputStream(jarFile)) {
            return createJarClassResolver(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JarClassResolver createJarClassResolver(InputStream inputStream) {
        try {
            JarClassResolver jarClassResolver = new JarClassResolver(inputStream);
            jarClassResolver.initialize();
            return jarClassResolver;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static AgentClassResolver createOnlineClassResolver(ClassLoader classLoader) {
        AgentClassResolver agentClassResolver = new AgentClassResolver(classLoader, commonClassesCache);

        loadCommonClassCacheFor(classLoader);

        return agentClassResolver;
    }

    public static void addClassData(String internalName, byte[] data) {
        Objects.requireNonNull(internalName);
        Objects.requireNonNull(data);

        internalName = Utils.dotToSlash(internalName);

        commonClassesCache.putIfAbsent(internalName, data);
    }

    private static void loadCommonClassCacheFor(ClassLoader classLoader) {
        if (classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            if (urls != null) {
                for (URL url : urls) {
                    if ("jar".equals(url.getProtocol())) {
                        new Thread(() -> {
                            try {
                                InputStream inputStream = url.openStream();
                                JarClassResolver jarClassResolver = ClassResolverFactory.createJarClassResolver(inputStream);

                                jarClassResolver.getClasses().forEach(commonClassesCache::putIfAbsent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
            }
        }
    }
}
