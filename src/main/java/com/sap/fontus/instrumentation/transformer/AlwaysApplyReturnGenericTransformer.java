package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;

public class AlwaysApplyReturnGenericTransformer implements ReturnTransformation{
    @Override
    public void transform(MethodTaintingVisitor visitor, Descriptor desc) {

    }
}
