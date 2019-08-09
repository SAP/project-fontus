package de.tubs.cs.ias.asm_test.strategies.clazz;

import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.TriConsumer;
import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

public interface ClassInstrumentationStrategy {

    Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor,
                                                      String signature, Object value, TriConsumer tc);

    Descriptor instrument(Descriptor desc);
}
