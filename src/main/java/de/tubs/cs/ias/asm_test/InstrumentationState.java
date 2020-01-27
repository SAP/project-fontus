package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;


public final class InstrumentationState {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final InstrumentationState instance = initializeState();

    private static InstrumentationState initializeState() {
        return new InstrumentationState();
    }

    private InstrumentationState() {
        this.annotations = new HashSet<>();
        this.noAnnotations = new HashSet<>();
    }

    public void addAnnotation(String name) {
        this.annotations.add(name);
    }

    public boolean isAnnotation(String name, ClassResolver resolver) {
        if (this.annotations.contains(name)) {
            return true;
        } else if (this.noAnnotations.contains(name)) {
            return false;
        } else {
            return this.testAnnotation(name, resolver);
        }
    }


    private boolean testAnnotation(String className, ClassResolver resolver) {
        try {
            NopVisitor nv = new NopVisitor(Opcodes.ASM7);
            ClassReader cr = new ClassReaderWithLoaderSupport(resolver, className);
            cr.accept(nv, ClassReader.SKIP_FRAMES);
            String clazzName = cr.getClassName();
            String superName = cr.getSuperName();
            String[] interfaces = cr.getInterfaces();
            String ifs = String.join(", ", interfaces);
            logger.info("Testing {} (super: {}, interfaces: {} for being an annotation", clazzName, superName, ifs);
            if (Utils.contains(interfaces, Constants.AnnotationQN)) {
                this.annotations.add(className);
                return true;
            }

            if (Constants.ProxyQN.equals(superName)) {
                if (interfaces.length == 1) {
                    String interf = interfaces[0];
                    if (this.testAnnotation(interf, resolver)) {
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

    private final Set<String> annotations;
    private final Set<String> noAnnotations;
}
