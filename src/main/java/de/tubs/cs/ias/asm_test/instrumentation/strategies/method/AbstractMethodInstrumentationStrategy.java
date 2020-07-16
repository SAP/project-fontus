package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.AbstractInstrumentation;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.util.HashMap;
import java.util.regex.Matcher;

public abstract class AbstractMethodInstrumentationStrategy extends AbstractInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private final HashMap<String, String> methodsToRename = new HashMap<>(1);
    private final Type type;
    private static final ParentLogger logger = LogUtils.getLogger();
    private final String taintedToOrig;
    private final TaintStringConfig taintStringConfig;

    AbstractMethodInstrumentationStrategy(MethodVisitor parentVisitor, String origDesc, String taintedDesc, String origQN, String taintedQN, String taintedToOrig, Class<?> type, TaintStringConfig taintStringConfig) {
        super(origDesc, taintedDesc, origQN, taintedQN);
        this.mv = parentVisitor;
        this.taintedToOrig = taintedToOrig;
        this.type = Type.getType(type);
        this.taintStringConfig = taintStringConfig;
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
    public void insertJdkMethodParameterConversion(String parameter) {
        Type paramType = Type.getType(parameter);
        if (this.type.equals(paramType)) {
            logger.info("Converting taint-aware {} to {} in multi param method invocation", this.origQN, this.origQN);
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.taintedQN, this.taintedToOrig, String.format("()%s", this.taintedDesc), false);
        }
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Type.getObjectType(owner).equals(this.type)) {
            String newDescriptor = InstrumentationHelper.getInstance(this.taintStringConfig).instrumentDesc(descriptor);
            String newOwner = this.taintedQN;
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(name, name);

            logger.info("Rewriting {} invoke [{}] {}.{}{} to {}.{}{}", this.origQN, Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
            this.mv.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            return true;
        }
        return false;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        Type returnType = Type.getReturnType(desc.toDescriptor());
        if (this.type.equals(returnType)) {
            logger.info("Converting returned {} of {}.{}{}", this.origQN, owner, name, desc.toDescriptor());
            this.origToTainted();
        }
    }

    private void origToTainted() {
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
            return this.instrumentQN(type);
        }
        return type;
    }
}
