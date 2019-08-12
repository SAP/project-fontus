package de.tubs.cs.ias.asm_test.strategies.clazz;

import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.TriConsumer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public class DefaultClassInstrumentationStrategy implements ClassInstrumentationStrategy {

    private final ClassVisitor visitor;

    public DefaultClassInstrumentationStrategy(ClassVisitor cv) {
        this.visitor = cv;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        FieldVisitor fv = this.visitor.visitField(access, name, descriptor, signature, value);
        return Optional.of(fv);
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc;
    }

    @Override
    public String instrumentQN(String qn) {
        return qn;
    }
}