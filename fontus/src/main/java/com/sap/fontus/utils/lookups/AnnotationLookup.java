package com.sap.fontus.utils.lookups;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sap.fontus.asm.ClassReaderWithLoaderSupport;
import com.sap.fontus.asm.resolver.IClassResolver;
import com.sap.fontus.asm.NopVisitor;
import com.sap.fontus.utils.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class AnnotationLookup {
    private static final Logger logger = LogUtils.getLogger();
    private final Cache<String, Boolean> cache;
    private final CombinedExcludedLookup combinedExcludedLookup;
    private final Map<String, Boolean> transformCache;

    private AnnotationLookup() {
        this.cache = Caffeine.newBuilder().build();
        this.transformCache = new ConcurrentHashMap<>();
        this.combinedExcludedLookup = new CombinedExcludedLookup();
    }

    public static AnnotationLookup getInstance() {
        return AnnotationLookup.LazyHolder.INSTANCE;
    }

    public boolean isAnnotation(String name, String superName, String[] interfaces, IClassResolver resolver) {
        if (name.startsWith("[")) {
            return false;
        } // Arrays ain't annotations
        return this.cache.get(name, (ignored) -> this.testAnnotation(name, superName, interfaces, resolver));
    }

    private boolean testAnnotation(String className, String superName, String[] interfaces, IClassResolver resolver) {
        Objects.requireNonNull(resolver);
        Objects.requireNonNull(className);
        boolean isAnnotation = false;

        try {
            NopVisitor nv = new NopVisitor(Opcodes.ASM9);
            ClassReader cr = new ClassReaderWithLoaderSupport(resolver, className);
            if (superName == null || interfaces == null) {
                cr.accept(nv, ClassReader.SKIP_FRAMES);
                superName = cr.getSuperName();
                interfaces = cr.getInterfaces();
            }

            // First check the annotation flag
            if ((cr.getAccess() & Opcodes.ACC_ANNOTATION) > 0) {
                isAnnotation = true;
            }

        } catch (IOException exception) {
            logger.warn("Could not read class {} for checking if it's an annotation", className);
        }
        if (interfaces == null) {
            interfaces = new String[0];
        }

        // Not necessary
        // Try again using the class via reflection
//        if (!isAnnotation) {
//            Class<?> cls = null;
//            try {
//                cls = Class.forName(className, false, null);
//            } catch (ClassNotFoundException ignored) {
//            }
//            if (cls != null) {
//                if (cls.isAnnotation()) {
//                    isAnnotation = true;
//                }
//            }
//        }

        // Not necessary anymore as every Class which was loaded was registered in the AnnotationLookup by the TaintingTransformer
        // Try again using the class via reflection
        if (!isAnnotation) {
            if (this.combinedExcludedLookup.isJdkClass(className)) {
                Class<?> cls = ClassUtils.findLoadedClass(className);
                if (cls != null) {
                    if (cls.isAnnotation()) {
                        isAnnotation = true;
                    }
                }
            } else {
                if (this.transformCache.containsKey(className)) {
                    isAnnotation = this.transformCache.get(className);
                }
            }
        }

        // Check the interfaces
        if (!isAnnotation) {
            for (String interf : interfaces) {
                if (this.testAnnotation(interf, null, null, resolver)) {
                    isAnnotation = true;
                    break;
                }
            }
        }

        if ((!isAnnotation) && (superName != null)) {
            if (this.testAnnotation(superName, null, null, resolver)) {
                isAnnotation = true;
            }
        }

        return isAnnotation;
    }


    public void addAnnotation(String name) {
        this.cache.put(name, true);
    }

    public boolean isAnnotation(String name, IClassResolver resolver) {
        return this.isAnnotation(name, null, null, resolver);
    }

    public void checkAnnotationAndCache(String className, byte[] classfileBuffer) {
        this.transformCache.computeIfAbsent(className, (ignored) -> {
            int access = new ClassReader(classfileBuffer).getAccess();
            return (access & Opcodes.ACC_ANNOTATION) > 0;
        });
    }

    private static class LazyHolder {
        private static final AnnotationLookup INSTANCE = new AnnotationLookup();
    }
}
