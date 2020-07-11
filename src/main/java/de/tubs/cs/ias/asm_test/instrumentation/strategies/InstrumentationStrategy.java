package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.asm.Descriptor;

import java.util.Optional;

public interface InstrumentationStrategy {
    Descriptor instrument(Descriptor desc);

    String instrumentQN(String qn);

    String instrumentDesc(String desc);

    Optional<String> translateClassName(String className);

    boolean handlesType(String typeName);
}
