package de.tubs.cs.ias.asm_test.instrumentation;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.ClassReaderWithLoaderSupport;
import de.tubs.cs.ias.asm_test.asm.ClassResolver;
import de.tubs.cs.ias.asm_test.asm.NopVisitor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public final class InstrumentationState {

    private static final ParentLogger logger = LogUtils.getLogger();

    private InstrumentationState() {
        this.annotations = new HashSet<>();
        this.noAnnotations = new HashSet<>();
    }

    public boolean isAnnotation(String name, String superName, String[] interfaces, ClassResolver resolver) {
        if(name.startsWith("[")) { return false; } // Arrays ain't annotations
        if (this.annotations.contains(name)) {
            return true;
        } else if (this.noAnnotations.contains(name)) {
            return false;
        } else {
            return this.testAnnotation(name, superName, interfaces, resolver);
        }
    }

    private static class LazyHolder {
        private static final InstrumentationState INSTANCE = new InstrumentationState();
    }

    public static InstrumentationState getInstance() {
        return InstrumentationState.LazyHolder.INSTANCE;
    }

    public void addAnnotation(String name) {
        this.annotations.add(name);
    }

    public boolean isAnnotation(String name, ClassResolver resolver) {
        return this.isAnnotation(name, null, null, resolver);
    }


    private boolean testAnnotation(String className, String superName, String[] interfaces, ClassResolver resolver) {
        try {
            if (superName == null || interfaces == null) {
                NopVisitor nv = new NopVisitor(Opcodes.ASM7);
                ClassReader cr = new ClassReaderWithLoaderSupport(resolver, className);
                cr.accept(nv, ClassReader.SKIP_FRAMES);
                superName = cr.getSuperName();
                interfaces = cr.getInterfaces();
            }
            String ifs = String.join(", ", interfaces);
            logger.info("Testing {} (super: {}, interfaces: {} for being an annotation", className, superName, ifs);
            if (Utils.contains(interfaces, Constants.AnnotationQN)) {
                this.annotations.add(className);
                return true;
            }

            if (Constants.ProxyQN.equals(superName)) {
                if (interfaces.length == 1) {
                    String interf = interfaces[0];
                    if (this.testAnnotation(interf, null, null, resolver)) {
                        this.annotations.add(className);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error resolving class {}", className, e);
        }
        this.noAnnotations.add(className);
        return false;
    }

    private final Set<String> noAnnotations;
    private final Set<String> annotations;
}
