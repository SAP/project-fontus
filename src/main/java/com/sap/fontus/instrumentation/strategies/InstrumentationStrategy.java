package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.asm.Descriptor;

import java.util.Optional;

public interface InstrumentationStrategy {
    /**
     * Replaces all taintable type occurrences with the concrete type
     */
    Descriptor instrumentForNormalCall(Descriptor desc);

    String instrumentQN(String qn);

    /**
     * Replaces taintable type occurences in the parameters with the interface and in the return with the concrete type
     * Used for instrumenting calls to taintable classes e.g. String/IASString.replaceAll
     */
    String instrumentDescForIASCall(String desc);

    Optional<String> translateClassName(String className);

    boolean handlesType(String descriptor);

    String getGetOriginalTypeMethod();
}
