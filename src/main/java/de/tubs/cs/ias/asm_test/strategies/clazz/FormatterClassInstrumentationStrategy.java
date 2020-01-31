package de.tubs.cs.ias.asm_test.strategies.clazz;

import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.strategies.StringInstrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public class FormatterClassInstrumentationStrategy extends StringInstrumentation implements ClassInstrumentationStrategy {
    private final ClassVisitor cl;

    public FormatterClassInstrumentationStrategy(ClassVisitor visitor) {
        this.cl = visitor;
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        return Optional.empty();
    }
}
