package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.config.TaintStringConfig;

import java.util.Optional;

public class DefaultInstrumentation implements InstrumentationStrategy {
    protected final TaintStringConfig stringConfig;

    public DefaultInstrumentation(TaintStringConfig configuration) {
        this.stringConfig = configuration;
    }

    @Override
    public Descriptor instrumentForNormalCall(Descriptor desc) {
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
    public String instrumentDescForIASCall(String desc) {
        return desc;
    }
}
