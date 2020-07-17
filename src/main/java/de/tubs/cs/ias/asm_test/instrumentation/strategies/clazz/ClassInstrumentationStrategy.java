package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationStrategy;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public interface ClassInstrumentationStrategy  {

    Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor,
                                                      String signature, Object value, TriConsumer tc);

    String instrumentSuperClass(String superClass);
}
