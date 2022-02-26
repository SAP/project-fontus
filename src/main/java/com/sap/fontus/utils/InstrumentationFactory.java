package com.sap.fontus.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.IClassResolver;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.offline.OfflineClassResolver;

public class InstrumentationFactory {
    private static final LoadingCache<ClassLoader, ClassResolver> classResolvers;
    private static final OfflineClassResolver offlineClassResolver;
    private static final ClassResolver nullResolver;

    static {
        classResolvers = Caffeine.newBuilder().build(ClassResolver::new);
        nullResolver = new ClassResolver(null);
        offlineClassResolver = new OfflineClassResolver(classResolvers.get(ClassLoader.getSystemClassLoader()));
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
}
