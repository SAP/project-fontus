package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class JdkMethodTransformer implements ParameterTransformation, ReturnTransformation {

    private final FunctionCall call;
    private final InstrumentationHelper instrumentationHelper;
    private final Configuration configuration;

    public JdkMethodTransformer(FunctionCall call, InstrumentationHelper instrumentationHelper, Configuration configuration) {
        this.call = call;
        this.instrumentationHelper = instrumentationHelper;
        this.configuration = configuration;
    }

    @Override
    public void transformParameter(int index, String typeString, MethodTaintingVisitor visitor) {
        this.instrumentationHelper.insertJdkMethodParameterConversion(visitor.getParentVisitor(), Type.getType(typeString));
    }

    @Override
    public boolean requireParameterTransformation(int index, String type) {
        return this.instrumentationHelper.needsJdkMethodParameterConversion(Type.getType(type));
    }

    @Override
    public void transformReturnValue(MethodTaintingVisitor visitor, Descriptor desc) {
        this.instrumentationHelper.instrumentStackTop(visitor.getParentVisitor(), Type.getType(desc.getReturnType()));

        FunctionCall converter = this.configuration.getConverterForReturnValue(this.call);
        if (converter != null) {
            visitor.visitMethodInsn(converter);
            return;
        }

        int returnSort = Type.getType(desc.getReturnType()).getSort();
        if (returnSort == Type.ARRAY || returnSort == Type.OBJECT) {
            FunctionCall convert = new FunctionCall(Opcodes.INVOKESTATIC,
                    Constants.ConversionUtilsQN,
                    Constants.ConversionUtilsToConcreteName,
                    Constants.ConversionUtilsToConcreteDesc,
                    false);
            visitor.visitMethodInsn(convert);
            visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(desc.getReturnType()).getInternalName());
        }
    }

    @Override
    public boolean requiresReturnTransformation(Descriptor desc) {
        return true;
    }

}
