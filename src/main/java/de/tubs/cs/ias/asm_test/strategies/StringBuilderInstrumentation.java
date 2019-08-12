package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;

public class StringBuilderInstrumentation implements InstrumentationStrategy {
    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringBuilderDesc, Constants.TStringBuilderDesc);

    }
}
