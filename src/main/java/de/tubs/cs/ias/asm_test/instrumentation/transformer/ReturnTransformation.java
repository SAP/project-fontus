package de.tubs.cs.ias.asm_test.instrumentation.transformer;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.instrumentation.MethodTaintingVisitor;

public interface ReturnTransformation {
    void transform(MethodTaintingVisitor visitor, Descriptor desc);
}
