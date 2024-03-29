package com.sap.fontus.utils;

import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassFinder {
    private final Map<String, ClassLoader> loadedClasses;
    private final Map<ClassLoader, Map<String, Class<?>>> cachePerClassLoader;
    private final Instrumentation instrumentation;
    private final CombinedExcludedLookup combinedExcludedLookup;

    public ClassFinder(Instrumentation instrumentation) {
        this.loadedClasses = new ConcurrentHashMap<>();
        this.cachePerClassLoader = new ConcurrentHashMap<>();
        this.instrumentation = instrumentation;
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    public Class<?> findClass(String name) {
        String className = Utils.slashToDot(name);
        String internalName = Utils.dotToSlash(name);
        if (this.combinedExcludedLookup.isJdkClass(internalName)) {
            try {
                return Class.forName(className, false, ClassLoader.getSystemClassLoader());
            } catch (ClassNotFoundException ignored) {
            }
        }

        // Bypass for offline and tests
        if (this.instrumentation == null) {
            try {
                return Class.forName(className, false, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        if (this.loadedClasses.containsKey(internalName)) {
            ClassLoader classLoader = this.loadedClasses.get(internalName);
            if (classLoader != null) {
                return this.findClassPerClassLoader(className, classLoader);
            } else {
                return this.findClassPerClassLoader(className, ClassLoader.getSystemClassLoader());
            }
        }

        return null;
    }

    private Class<?> findClassPerClassLoader(String className, ClassLoader classLoader) {
        if (!this.cachePerClassLoader.containsKey(classLoader)) {
            this.updateCacheForClassLoader(classLoader);
            Map<String, Class<?>> cache = this.cachePerClassLoader.get(classLoader);
            if (cache.containsKey(className)) {
                return cache.get(className);
            }
        } else {
            Map<String, Class<?>> cache = this.cachePerClassLoader.get(classLoader);

            if (!cache.containsKey(className)) {
                this.updateCacheForClassLoader(classLoader);
            }

            if (cache.containsKey(className)) {
                return cache.get(className);
            }
        }
        return null;
    }

    private void updateCacheForClassLoader(ClassLoader classLoader) {
        Class<?>[] classes = this.instrumentation.getInitiatedClasses(classLoader);

        this.cachePerClassLoader.putIfAbsent(classLoader, new ConcurrentHashMap<>());

        Map<String, Class<?>> cache = this.cachePerClassLoader.get(classLoader);

        if (classes != null) {
            for (Class<?> cls : classes) {
                cache.putIfAbsent(cls.getName(), cls);
            }
        }
    }

    public void addClass(String internalName, ClassLoader classLoader) {
        if(classLoader != null) {
            this.loadedClasses.putIfAbsent(internalName, classLoader);
        }
    }

    public boolean isClassLoaded(String internalName) {
        return this.loadedClasses.containsKey(internalName);
    }
}
