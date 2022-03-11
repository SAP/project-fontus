package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;

public interface ReturnTransformation {

    /**
     * Perform a transformation on the return value of a method
     * @param visitor The MethodVisitor used to add bytecodes
     * @param desc Method descriptor
     */
    void transformReturnValue(MethodTaintingVisitor visitor, Descriptor desc);

    /**
     * Does this transformation actually apply? In some cases a transformer might
     * be added but not apply to a method.
     * @param desc The Method descriptor
     * @return Does this transformation actually apply?
     */
    boolean requiresReturnTransformation(Descriptor desc);

    /**
     * Does the transformation require the input parameters to the method as local variables?
     * This is the case with a taint source, which requires the input parameters to have been added
     * in order to add them to the taint handler.
     *
     * In most cases this will be false. It only needs to be true if the transformer exepects
     * additional local variables to be present.
     *
     * @return
     */
    boolean requireParameterVariableLocals();

}
