package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.asm.Descriptor;

import java.util.Optional;

public interface InstrumentationStrategy {
    Descriptor instrument(Descriptor desc);

    String instrumentQN(String qn);

    String instrumentDesc(String desc);

    Optional<String> translateClassName(String className);

    boolean handlesType(String typeName);

    String getGetOriginalTypeMethod();
}
