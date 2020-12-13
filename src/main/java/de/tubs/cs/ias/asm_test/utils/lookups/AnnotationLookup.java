package de.tubs.cs.ias.asm_test.utils.lookups;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.ClassReaderWithLoaderSupport;
import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.asm.NopVisitor;
import de.tubs.cs.ias.asm_test.utils.ClassUtils;
import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public final class AnnotationLookup {

    private static final ParentLogger logger = LogUtils.getLogger();

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
            return this.testAnnotation(name, superName, interfaces, resolver);
        }
    }

    private boolean testAnnotation(String className, String superName, String[] interfaces, ClassResolver resolver) {
        Objects.requireNonNull(resolver);
        Objects.requireNonNull(className);
        boolean isAnnotation = false;

        try {
            NopVisitor nv = new NopVisitor(Opcodes.ASM7);
            ClassReader cr = new ClassReaderWithLoaderSupport(resolver, className);
            if (superName == null || interfaces == null) {
                cr.accept(nv, ClassReader.SKIP_FRAMES);
                superName = cr.getSuperName();
                interfaces = cr.getInterfaces();
            }

            if (!isAnnotation) {
                if ((cr.getAccess() & Opcodes.ACC_ANNOTATION) > 0) {
                    isAnnotation = true;
                }
            }
        } catch (IOException exception) {
            logger.warn("Could not read class {} for checking if it's an annotation", className);
        }
        if (interfaces == null) {
            interfaces = new String[0];
        }

        if (!isAnnotation) {
            Class<?> cls = ClassUtils.findLoadedClass(className);
            if (cls != null) {
                if (cls.isAnnotation()) {
                    isAnnotation = true;
                }
            }
        }

        if (!isAnnotation) {
            String ifs = String.join(", ", interfaces);
            logger.info("Testing {} (super: {}, interfaces: {} for being an annotation", className, superName, ifs);
            if (Utils.contains(interfaces, Constants.AnnotationQN)) {
                isAnnotation = true;
            }
        }

        if (!isAnnotation) {
            if (Constants.ProxyQN.equals(superName)) {
                if (interfaces.length == 1) {
                    String interf = interfaces[0];
                    if (this.testAnnotation(interf, null, null, resolver)) {
                        isAnnotation = true;
                    }
                }
            }
        }

        if (isAnnotation) {
            this.annotations.add(className);
        } else {
            this.noAnnotations.add(className);
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
