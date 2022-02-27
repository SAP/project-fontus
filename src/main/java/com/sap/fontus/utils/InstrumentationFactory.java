package com.sap.fontus.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.IClassResolver;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.offline.OfflineClassResolver;

public class InstrumentationFactory {
    private static final LoadingCache<ClassLoader, ClassResolver> classResolvers;
    private static final OfflineClassResolver offlineClassResolver;
    private static final ClassResolver nullResolver;
    private static final ClassFinder classFinder;

    static {
        classResolvers = Caffeine.newBuilder().build(ClassResolver::new);
        nullResolver = new ClassResolver(null);
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


}
