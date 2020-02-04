package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.MethodTaintingVisitor;

public interface ReturnTransformation {
    void transform(MethodTaintingVisitor visitor, Descriptor desc);
}
