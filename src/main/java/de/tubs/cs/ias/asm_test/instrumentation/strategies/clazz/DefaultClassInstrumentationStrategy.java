package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.DefaultInstrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public class DefaultClassInstrumentationStrategy implements ClassInstrumentationStrategy {

    private final ClassVisitor visitor;

    public DefaultClassInstrumentationStrategy(ClassVisitor cv, TaintStringConfig configuration) {
        this.visitor = cv;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        FieldVisitor fv = this.visitor.visitField(access, name, descriptor, signature, value);
        return Optional.of(fv);
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        return superClass;
    }
}