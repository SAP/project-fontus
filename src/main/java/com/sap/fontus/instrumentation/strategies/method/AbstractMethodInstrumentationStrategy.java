package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.instrumentation.strategies.InstrumentationStrategy;
import com.sap.fontus.instrumentation.strategies.InstrumentationHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMethodInstrumentationStrategy implements MethodInstrumentationStrategy {
    protected final MethodVisitor mv;
    protected final HashMap<String, String> methodsToRename = new HashMap<>(1);
    protected final Type type;
    protected static final Logger logger = LogUtils.getLogger();
    protected final String taintedToOrig;
    protected final TaintStringConfig stringConfig;
    protected final Pattern descPattern;
    protected final String taintedDesc;
    protected final String taintedQN;
    protected final String origQN;
    protected final String origDesc;
    protected final InstrumentationStrategy instrumentationStrategy;
    protected final Pattern qnPattern;

    AbstractMethodInstrumentationStrategy(MethodVisitor parentVisitor, String taintedDesc, String taintedQN, String taintedToOrig, Class<?> type, TaintStringConfig taintStringConfig, InstrumentationStrategy instrumentationStrategy) {
        this.mv = parentVisitor;
        this.taintedToOrig = taintedToOrig;
        this.type = Type.getType(type);
        this.origQN = this.type.getInternalName();
        this.origDesc = this.type.getDescriptor();
        this.stringConfig = taintStringConfig;
        this.descPattern = Pattern.compile(origDesc);
        this.qnPattern = Pattern.compile(origQN);
        this.taintedDesc = taintedDesc;
        this.taintedQN = taintedQN;
        this.instrumentationStrategy = instrumentationStrategy;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    AbstractMethodInstrumentationStrategy(MethodVisitor parentVisitor, Class<?> taintedClass, Class<?> origClass, String taintedToOrig, TaintStringConfig taintStringConfig, InstrumentationStrategy instrumentationStrategy) {
        this.mv = parentVisitor;
        this.taintedToOrig = taintedToOrig;
        this.type = Type.getType(origClass);
        this.origQN = this.type.getInternalName();
        this.origDesc = this.type.getDescriptor();
        this.stringConfig = taintStringConfig;
        this.descPattern = Pattern.compile(origDesc);
        this.qnPattern = Pattern.compile(origQN);
        this.taintedDesc = Type.getType(taintedClass).getDescriptor();
        this.taintedQN = Type.getType(taintedClass).getInternalName();
        this.instrumentationStrategy = instrumentationStrategy;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = this.descPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(this.taintedDesc);
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }

    @Override
    public boolean insertJdkMethodParameterConversion(String parameter) {
        Type paramType = Type.getType(parameter);
        if (this.type.equals(paramType)) {
            logger.info("Converting taint-aware {} to {} in multi param method invocation", this.origQN, this.origQN);
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.taintedQN, this.taintedToOrig, String.format("()%s", this.origDesc), false);
            return true;
        }
        return false;
    }

    @Override
    public FunctionCall rewriteOwnerMethod(FunctionCall functionCall) {
        if (Type.getObjectType(functionCall.getOwner()).equals(this.type)) {
            String newDescriptor = InstrumentationHelper.getInstance(this.stringConfig).instrumentDescForIASCall(functionCall.getDescriptor());
            String newOwner = this.taintedQN;
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(functionCall.getName(), functionCall.getName());

            logger.info("Rewriting {} invoke [{}] {}.{}{} to {}.{}{}", this.origQN, Utils.opcodeToString(functionCall.getOpcode()), functionCall.getOwner(), functionCall.getName(), functionCall.getName(), newOwner, newName, newDescriptor);
            return new FunctionCall(functionCall.getOpcode(), newOwner, newName, newDescriptor, functionCall.isInterface());
        }
        return null;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        Type returnType = Type.getReturnType(desc.toDescriptor());
        if (this.type.equals(returnType)) {
            logger.info("Converting returned {} of {}.{}{}", this.origQN, owner, name, desc.toDescriptor());
            this.origToTainted();
        }
    }

    protected void origToTainted() {
        this.mv.visitTypeInsn(Opcodes.NEW, this.taintedQN);
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.taintedQN, Constants.Init, String.format("(%s)V", this.origDesc), false);
    }

    @Override
    public boolean handleLdc(Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        if (this.type.equals(type)) {
            this.mv.visitLdcInsn(Type.getObjectType(this.taintedQN));
            return true;
        }
        return false;
    }

    @Override
    public boolean handleLdcArray(Type type) {
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        boolean isArray = type.startsWith("[");
        if (Type.getObjectType(type).equals(this.type) || (isArray && type.endsWith(this.origDesc))) {
            return this.instrumentationStrategy.instrumentQN(type);
        }
        return type;
    }
}
