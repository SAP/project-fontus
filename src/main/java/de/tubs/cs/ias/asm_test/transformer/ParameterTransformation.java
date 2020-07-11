package de.tubs.cs.ias.asm_test.transformer;

import de.tubs.cs.ias.asm_test.instrumentation.MethodTaintingVisitor;

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
    void transform(int index, String type, MethodTaintingVisitor visitor);
}
