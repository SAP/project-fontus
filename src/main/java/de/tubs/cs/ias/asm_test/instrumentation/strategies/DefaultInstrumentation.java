package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

import java.util.Optional;

public class DefaultInstrumentation implements InstrumentationStrategy {
    protected final TaintStringConfig stringConfig;

    public DefaultInstrumentation(TaintStringConfig configuration) {
        this.stringConfig = configuration;
    }

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
    public String getGetOriginalTypeMethod() {
        return null;
    }

    @Override
    public String instrumentDesc(String desc) {
        return desc;
    }
}
