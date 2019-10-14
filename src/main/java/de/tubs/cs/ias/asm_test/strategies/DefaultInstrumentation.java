package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Descriptor;

import java.util.Optional;

public class DefaultInstrumentation implements InstrumentationStrategy {
    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc;
    }

    @Override
    public String instrumentQN(String qn) {
        return qn;
    }

    @Override
    public Optional<String> translateClassName(String className) {
        return Optional.of(className);
    }

    @Override
    public boolean handlesType(String typeName) {
        return false;
    }

    @Override
    public String instrumentDesc(String desc) {
        return desc;
    }
}
