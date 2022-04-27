package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.instrumentation.MethodTaintingVisitor;

public interface ParameterTransformation {

    /**
     * Called for each parameter in a method.
     * <p>
     * The implementation should decide whether a transformation needs to take
     * place, and if so, can implement it as necessary. The caller should ensure
     * that the stack is left with the same number of entries as before, otherwise
     * the storage will fail.
     *
     * @param index The parameter index being transformed
     * @param type The type descriptor of the parameter
     * @param visitor The method visitor
     */
    void transformParameter(int index, String type, MethodTaintingVisitor visitor);

    /**
     * Does the given parameter actually need transforming? In some cases the transformation
     * may not apply, if no operation applies at all, we can reduce the number of bytecode
     * operations required
     * @param index The index of the parameter for this method
     * @param type The type of parameter
     * @return Does the transformation apply
     */
    boolean requireParameterTransformation(int index, String type);

}
