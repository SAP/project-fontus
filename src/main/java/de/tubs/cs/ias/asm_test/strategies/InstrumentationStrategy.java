package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Descriptor;

public interface InstrumentationStrategy {
    Descriptor instrument(Descriptor desc);
}
