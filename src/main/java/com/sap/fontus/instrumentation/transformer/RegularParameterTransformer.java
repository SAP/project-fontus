package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;

public class RegularParameterTransformer implements ParameterTransformation {

    private final FunctionCall call;
    private final Configuration configuration;

    public RegularParameterTransformer(FunctionCall call, InstrumentationHelper instrumentationHelper, Configuration configuration) {
        this.call = call;
        this.configuration = configuration;
    }

    @Override
    public void transform(int index, String type, MethodTaintingVisitor visitor) {
        FunctionCall converter = this.configuration.getConverterForParameter(this.call, index);
        if (converter != null) {
            visitor.visitMethodInsn(converter);
            return;
        }
    }
}
