package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.logging.LogManager;

public class JdkMethodTransformer implements ParameterTransformation, ReturnTransformation {

    /**
     * TODO: This is really ugly and should probably be changed to something more generic, just an attempt to cut down on excessive ConversionUtils usage
     */
    private static final Set<Type> neverConvert = Set.of(
            Type.getType(Integer.class),
            Type.getType(Long.class),
            Type.getType(Boolean.class),
            Type.getType(Float.class),
            Type.getType(Double.class),
            Type.getType(Short.class),
            Type.getType(Byte.class),
            Type.getType(Character.class)
    );
    private static final Set<Type> harmlessOwners = Set.of(
            Type.getType(List.class),
            Type.getType(Set.class),
            Type.getType(Arrays.class),
            Type.getType(Map.class),
            Type.getType(ArrayList.class),
            Type.getType(Collections.class),
            Type.getType(Collection.class),
            Type.getType(Queue.class),
            Type.getType(Deque.class),
            Type.getType(Hashtable.class),
            Type.getType(LogManager.class),
            Type.getType(Stack.class)
    );
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
        Type returnType = Type.getType(desc.getReturnType());
        returnType = this.instrumentationHelper.instrumentStackTop(visitor.getParentVisitor(), returnType);

        FunctionCall converter = this.configuration.getConverterForReturnValue(this.call);
        if (converter != null) {
            visitor.visitMethodInsn(converter);
            return;
        }


        int returnSort = returnType.getSort();
        if (!returnType.getClassName().startsWith(Constants.TAINTAWARE_PACKAGE_NAME) && ((returnSort == Type.ARRAY && (returnType.getElementType().getSort() >= Type.ARRAY && !neverConvert.contains(returnType.getElementType()))) || returnSort == Type.OBJECT && !neverConvert.contains(returnType))
            // TODO: evaluate soundness:        &&!(this.call.getOwner().equals("java/util/Iterator") && this.call.getName().equals("next"))
                && !harmlessOwners.contains(returnType)
        ) {
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

    @Override
    public boolean requireParameterVariableLocals() {
        return false;
    }

}
