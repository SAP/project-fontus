package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Descriptor;

import java.util.ArrayList;
import java.util.Collection;

public class DescriptorInstrumenter {
    private static final Collection<InstrumentationStrategy> strategies = new ArrayList<>(3);

    static {
        strategies.add(new StringInstrumentation());
        strategies.add(new StringBuilderInstrumentation());
        strategies.add(new StringBufferInstrumentation());
    }

    public static Descriptor instrument(Descriptor desc) {
        Descriptor newDesc = desc;
        for(InstrumentationStrategy is : strategies) {
            newDesc = is.instrument(newDesc);
        }
        return newDesc;
    }

}
