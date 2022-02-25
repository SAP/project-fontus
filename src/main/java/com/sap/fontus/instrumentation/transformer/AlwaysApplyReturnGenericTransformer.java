package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;

public class AlwaysApplyReturnGenericTransformer implements ReturnTransformation {
    private final FunctionCall converter;

    public AlwaysApplyReturnGenericTransformer(FunctionCall converter) {
        this.converter = converter;
    }

    @Override
    public void transformReturnValue(MethodTaintingVisitor visitor, Descriptor desc) {
        visitor.visitMethodInsn(converter);
    }

    @Override
    public boolean requiresReturnTransformation(Descriptor desc) {
        return true;
    }
}
