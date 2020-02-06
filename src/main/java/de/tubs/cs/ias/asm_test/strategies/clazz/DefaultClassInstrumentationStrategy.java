package de.tubs.cs.ias.asm_test.strategies.clazz;

import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.strategies.DefaultInstrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public class DefaultClassInstrumentationStrategy extends DefaultInstrumentation implements ClassInstrumentationStrategy {

    private final ClassVisitor visitor;

    public DefaultClassInstrumentationStrategy(ClassVisitor cv, TaintStringConfig configuration) {
        super(configuration);
        this.visitor = cv;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        FieldVisitor fv = this.visitor.visitField(access, name, descriptor, signature, value);
        return Optional.of(fv);
    }
}