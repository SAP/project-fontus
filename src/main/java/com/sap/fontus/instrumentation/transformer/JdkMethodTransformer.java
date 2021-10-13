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
    public void transform(int index, String typeString, MethodTaintingVisitor visitor) {
        Type type = Type.getType(typeString);

        if (this.instrumentationHelper.insertJdkMethodParameterConversion(visitor.getParentVisitor(), type)) {
            return;
        }

        FunctionCall converter = this.configuration.getConverterForParameter(this.call, index);
        if (converter != null) {
            visitor.visitMethodInsn(converter);
            return;
        }

//        Type type = Type.getType(typeString);
//        int returnSort = type.getSort();
//        if (returnSort == Type.ARRAY || returnSort == Type.OBJECT) {
//            Utils.insertGenericConversionToOrig(visitor.getParent(), type.getInternalName());
//        }
    }

    @Override
    public void transform(MethodTaintingVisitor visitor, Descriptor desc) {
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

}
