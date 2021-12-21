package com.sap.fontus.utils.lookups;

import com.sap.fontus.asm.ClassReaderWithLoaderSupport;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.NopVisitor;
import com.sap.fontus.utils.ClassUtils;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class AnnotationLookup {

    private static final Logger logger = LogUtils.getLogger();

    private AnnotationLookup() {
        this.annotations = new HashSet<>();
        this.noAnnotations = new HashSet<>();
    }

    public static AnnotationLookup getInstance() {
        return AnnotationLookup.LazyHolder.INSTANCE;
    }

    public boolean isAnnotation(String name, String superName, String[] interfaces, ClassResolver resolver) {
        if (name.startsWith("[")) {
            return false;
        } // Arrays ain't annotations
        if (this.annotations.contains(name)) {
            return true;
        } else if (this.noAnnotations.contains(name)) {
            return false;
        } else {
            return this.testAndCacheAnnotation(name, superName, interfaces, resolver);
        }
    }

    // Deal with caching stuff here
    private boolean testAndCacheAnnotation(String name, String superName, String[] interfaces, ClassResolver resolver) {
        if (this.annotations.contains(name)) {
            return true;
        } else if (this.noAnnotations.contains(name)) {
            return false;
        } else {
            boolean isAnnotation = this.testAnnotation(name, superName, interfaces, resolver);
            // Store in cache
            if (isAnnotation) {
                this.annotations.add(name);
            } else {
                this.noAnnotations.add(name);
            }
            return isAnnotation;
        }
    }

    private boolean testAnnotation(String className, String superName, String[] interfaces, ClassResolver resolver) {
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
                if (this.testAndCacheAnnotation(interf, null, null, resolver)) {
                    isAnnotation = true;
                    break;
                }
            }
        }

        if ((!isAnnotation) && (superName != null)) {
            if (this.testAndCacheAnnotation(superName, null, null, resolver)) {
                isAnnotation = true;
            }
        }

        return isAnnotation;
    }


    public void addAnnotation(String name) {
        this.annotations.add(name);
    }

    public boolean isAnnotation(String name, ClassResolver resolver) {
        return this.isAnnotation(name, null, null, resolver);
    }

    private static class LazyHolder {
        private static final AnnotationLookup INSTANCE = new AnnotationLookup();
    }

    private final Set<String> noAnnotations;
    private final Set<String> annotations;
}
