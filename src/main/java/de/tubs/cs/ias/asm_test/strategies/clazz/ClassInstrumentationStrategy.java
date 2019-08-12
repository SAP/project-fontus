package de.tubs.cs.ias.asm_test.strategies.clazz;

import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.strategies.InstrumentationStrategy;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public interface ClassInstrumentationStrategy extends InstrumentationStrategy {

    Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor,
                                                      String signature, Object value, TriConsumer tc);
}
