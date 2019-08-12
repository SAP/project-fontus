package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Descriptor;

import java.util.Optional;

public interface InstrumentationStrategy {
    Descriptor instrument(Descriptor desc);
    String instrumentQN(String qn);
    Optional<String> translateClassName(String className);
    boolean handlesType(String typeName);
}
