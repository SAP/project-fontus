package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.FieldVisitor;

import java.util.Optional;

interface ClassInstrumentationStrategy {

    Optional<FieldVisitor> instrumentFieldInstruction(int access, String name, String descriptor,
                                                      String signature, Object value, TriConsumer tc);

    Descriptor instrumentMethodInvocation(Descriptor desc);
}
