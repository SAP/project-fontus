package de.tubs.cs.ias.asm_test.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;

public class StringInstrumentation {
    public Descriptor instrument(Descriptor desc) {
        return desc.replaceType(Constants.StringDesc, Constants.TStringDesc);
    }
}
