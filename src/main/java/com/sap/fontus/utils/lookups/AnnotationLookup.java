package com.sap.fontus.utils.lookups;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.sap.fontus.asm.ClassReaderWithLoaderSupport;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.IClassResolver;
import com.sap.fontus.asm.NopVisitor;
import com.sap.fontus.utils.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AnnotationLookup {
    private static final Logger logger = LogUtils.getLogger();
    private final Cache<String, Boolean> cache;

    private AnnotationLookup() {
        this.cache = Caffeine.newBuilder().build();
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

        // Try again using the class via reflection
        if (!isAnnotation) {
            Class<?> cls = null;
            try {
                cls = Class.forName(className, false, null);
            } catch (ClassNotFoundException ignored) {
            }
            if (cls != null) {
                if (cls.isAnnotation()) {
                    isAnnotation = true;
                }
            }
        }

        // Try again using the class via reflection
        if (!isAnnotation) {
            Class<?> cls = ClassUtils.findLoadedClass(className);
            if (cls != null) {
                if (cls.isAnnotation()) {
                    isAnnotation = true;
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

    private static class LazyHolder {
        private static final AnnotationLookup INSTANCE = new AnnotationLookup();
    }
}
