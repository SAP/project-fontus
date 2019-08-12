package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Descriptor;

import java.util.ArrayList;
import java.util.Collection;

public class QualifiedNameInstrumenter {
    private static final Collection<InstrumentationStrategy> strategies = new ArrayList<>(4);

    static {
        strategies.add(new StringInstrumentation());
        strategies.add(new StringBuilderInstrumentation());
        strategies.add(new StringBufferInstrumentation());
        strategies.add(new DefaultInstrumentation());
    }

    public static String instrumentQN(String qn) {
        String newQN = qn;
        for(InstrumentationStrategy is : strategies) {
            newQN = is.instrumentQN(newQN);
        }
        return newQN;
    }

}
