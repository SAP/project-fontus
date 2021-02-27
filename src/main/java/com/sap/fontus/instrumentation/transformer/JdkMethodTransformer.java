package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.instrumentation.strategies.method.MethodInstrumentationStrategy;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Collection;

public class JdkMethodTransformer implements ParameterTransformation, ReturnTransformation {

    private final FunctionCall call;
    private final Collection<MethodInstrumentationStrategy> instrumentation;
    private final Configuration configuration;

    public JdkMethodTransformer(FunctionCall call, Collection<MethodInstrumentationStrategy> instrumentation, Configuration configuration) {
        this.call = call;
        this.instrumentation = instrumentation;
        this.configuration = configuration;
    }

    @Override
    public void transform(int index, String typeString, MethodTaintingVisitor visitor) {

        for (MethodInstrumentationStrategy s : this.instrumentation) {
            if (s.insertJdkMethodParameterConversion(typeString)) {
                return;
            }
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

        for (MethodInstrumentationStrategy s : this.instrumentation) {
            s.instrumentReturnType(this.call.getOwner(), this.call.getName(), desc);
        }

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
