package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;

public interface ReturnTransformation {

    void transformReturnValue(MethodTaintingVisitor visitor, Descriptor desc);

    boolean requiresReturnTransformation(Descriptor desc);

}
