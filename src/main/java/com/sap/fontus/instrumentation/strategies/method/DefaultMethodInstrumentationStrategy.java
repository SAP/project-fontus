package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.MethodUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DefaultMethodInstrumentationStrategy implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private static final Logger logger = LogUtils.getLogger();
    private static final Set<Type> requireValueOf = fillRequireValueOfSet();
    private final TaintStringConfig stringConfig;

    public DefaultMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig stringConfig) {
        this.stringConfig = stringConfig;
        this.mv = mv;
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
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        this.mv.visitFieldInsn(opcode, owner, name, descriptor);
        return true;
    }

    @Override
    public boolean insertJdkMethodParameterConversion(String parameter) {
        return false;
    }

    @Override
    public FunctionCall rewriteOwnerMethod(FunctionCall functionCall) {
        Type tOwner = Type.getObjectType(functionCall.getOwner());
        if (MethodUtils.isToString(functionCall.getName(), functionCall.getDescriptor()) && requireValueOf.contains(tOwner) && functionCall.getOpcode() != Opcodes.INVOKESPECIAL) {
            int newOpcode = Opcodes.INVOKESTATIC;
            String newOwner = this.stringConfig.getTStringQN();
            String newDescriptor = "(" + Constants.ObjectDesc + ")" + this.stringConfig.getTStringDesc();
            String newName = Constants.TO_STRING_OF;
            boolean newIsInterface = false;
            logger.info("Rewriting toString invoke [{}] {}.{}{} to valueOf call {}.{}{}", Utils.opcodeToString(functionCall.getOpcode()), functionCall.getOwner(), functionCall.getName(), functionCall.getDescriptor(), newOwner, newName, newDescriptor);
            return new FunctionCall(newOpcode, newOwner, newName, newDescriptor, false);
        }
        return null;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
    }

    @Override
    public boolean handleLdc(Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        return false;
    }

    @Override
    public boolean handleLdcArray(Type type) {
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        return type;
    }
}
