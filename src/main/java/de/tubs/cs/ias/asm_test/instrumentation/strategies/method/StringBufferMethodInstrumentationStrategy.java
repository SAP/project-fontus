package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.StringBufferInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import de.tubs.cs.ias.asm_test.utils.Logger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;


public class StringBufferMethodInstrumentationStrategy extends StringBufferInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private static final Logger logger = LogUtils.getLogger();
    private final HashMap<String, String> methodsToRename = new HashMap<>(1);
    private static final Type stringBufferType = Type.getType(StringBuffer.class);


    public StringBufferMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(configuration);
        this.mv = mv;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    private void stringBufferToTStringBuffer() {
        this.mv.visitTypeInsn(Opcodes.NEW, this.stringConfig.getTStringBufferQN());
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.stringConfig.getTStringBufferQN(), Constants.Init, String.format("(%s)V", Constants.StringBufferDesc), false);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = Constants.strBufferPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(this.stringConfig.getTStringBufferDesc());
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
        Type paramType = Type.getType(parameter);
        if (stringBufferType.equals(paramType)) {
            logger.info("Converting taint-aware StringBuffer to StringBuffer in multi param method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.stringConfig.getTStringBufferQN(), Constants.TStringBufferToStringBufferName, String.format("()%s", Constants.StringBufferDesc), false);
        }
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        Type returnType = Type.getReturnType(desc.toDescriptor());
        if (stringBufferType.equals(returnType)) {
            logger.info("Converting returned StringBuffer of {}.{}{}", owner, name, desc.toDescriptor());
            this.stringBufferToTStringBuffer();
        }
    }

    @Override
    public boolean handleLdc(Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        if (stringBufferType.equals(type)) {
            this.mv.visitLdcInsn(Type.getObjectType(this.stringConfig.getTStringBufferQN()));
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
        if (Type.getObjectType(type).equals(stringBufferType) || (isArray && type.endsWith(Constants.StringBufferDesc))) {
            return this.instrumentQN(type);
        }
        return type;
    }


    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Type.getObjectType(owner).equals(stringBufferType)) {
            String newDescriptor = InstrumentationHelper.getInstance(this.stringConfig).instrumentDesc(descriptor);
            String newOwner = this.stringConfig.getTStringBufferQN();
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(name, name);

            logger.info("Rewriting StringBuffer invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
            this.mv.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            return true;
        }
        return false;
    }
}
