package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.TriConsumer;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.MethodUtils;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DefaultInstrumentation implements InstrumentationStrategy {
    private static final Logger logger = LogUtils.getLogger();
    private static final Set<Type> requireValueOf = fillRequireValueOfSet();

    public DefaultInstrumentation() {
    }

    private static Set<Type> fillRequireValueOfSet() {
        Set<Type> set = new HashSet<>();
        set.add(Type.getType(CharSequence.class));
        set.add(Type.getType(Object.class));
        set.add(Type.getType(Serializable.class));
        set.add(Type.getType(Appendable.class));
        return set;
    }

    @Override
    public Descriptor instrument(Descriptor desc) {
        return desc;
    }

    @Override
    public String uninstrument(String typeDescriptor) {
        return typeDescriptor;
    }

    @Override
    public String instrument(String typeDescriptor) {
        return typeDescriptor;
    }

    @Override
    public Descriptor uninstrumentForJdkCall(Descriptor descriptor) {
        return descriptor;
    }

    @Override
    public String instrumentQN(String qn) {
        return qn;
    }

    @Override
    public String uninstrumentQN(String qn) {
        return qn;
    }

    @Override
    public Optional<String> translateClassName(String className) {
        return Optional.of(className);
    }

    @Override
    public boolean handlesType(String descriptor) {
        return false;
    }

    @Override
    public boolean isInstrumented(String descriptor) {
        return false;
    }


    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(ClassVisitor classVisitor, int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        FieldVisitor fv = classVisitor.visitField(access, name, descriptor, signature, value);
        return Optional.of(fv);
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        return superClass;
    }

    @Override
    public boolean instrumentFieldIns(MethodVisitor mv, int opcode, String owner, String name, String descriptor) {
        mv.visitFieldInsn(opcode, owner, name, descriptor);
        return true;
    }

    @Override
    public boolean insertJdkMethodParameterConversion(MethodVisitor mv, Type parameter) {
        return false;
    }

    @Override
    public boolean insertJdkMethodParameterConversion(MethodVisitor mv, Type source, Type parameter) {
        return false;
    }

    @Override
    public boolean needsJdkMethodParameterConversion(Type parameter) {
        return false;
    }

    @Override
    public FunctionCall rewriteOwnerMethod(FunctionCall functionCall) {
        Type tOwner = Type.getObjectType(functionCall.getOwner());
        if (MethodUtils.isToString(functionCall.getName(), functionCall.getDescriptor()) && requireValueOf.contains(tOwner) && functionCall.getOpcode() != Opcodes.INVOKESPECIAL) {
            int newOpcode = Opcodes.INVOKESTATIC;
            String newOwner = Type.getType(IASString.class).getInternalName();
            String newDescriptor = "(" + Constants.ObjectDesc + ")" + Type.getType(IASString.class).getDescriptor();
            String newName = Constants.TO_STRING_OF;
            if(LogUtils.LOGGING_ENABLED) {
                logger.info("Rewriting toString invoke [{}] {}.{}{} to valueOf call {}.{}{}", Utils.opcodeToString(functionCall.getOpcode()), functionCall.getOwner(), functionCall.getName(), functionCall.getDescriptor(), newOwner, newName, newDescriptor);
            }
            return new FunctionCall(newOpcode, newOwner, newName, newDescriptor, false);
        }
        return null;
    }

    @Override
    public Type instrumentStackTop(MethodVisitor mv, Type origType) {
        return null;
    }

    @Override
    public boolean handleLdc(MethodVisitor mv, Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(MethodVisitor mv, Type type) {
        return false;
    }

    @Override
    public boolean handleLdcArray(MethodVisitor mv, Type type) {
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        return type;
    }
}
