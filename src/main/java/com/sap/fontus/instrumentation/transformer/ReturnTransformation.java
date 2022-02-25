package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;

public interface ReturnTransformation {

    /* Perform a transformation on the return value of a method */
    void transformReturnValue(MethodTaintingVisitor visitor, Descriptor desc);

    /* Is a transformation needed */
    boolean requiresReturnTransformation(Descriptor desc);

    /* Does the transformation require the input parameters to the method as local variables? e.g. as with source */
    boolean requireParameterVariableLocals();

}
