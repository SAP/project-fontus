package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.StringBuilderInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;

public class StringBuilderMethodInstrumentationStrategy extends StringBuilderInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final HashMap<String, String> methodsToRename = new HashMap<>(1);
    private static final Type stringBuilderType = Type.getType(StringBuilder.class);

    public StringBuilderMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(configuration);
        this.mv = mv;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    private void stringBuilderToTStringBuilder() {
        this.mv.visitTypeInsn(Opcodes.NEW, this.stringConfig.getTStringBuilderQN());
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.stringConfig.getTStringBuilderQN(), Constants.Init, String.format("(%s)V", Constants.StringBuilderDesc), false);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = Constants.strBuilderPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(this.stringConfig.getTStringBuilderDesc());
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
        Type paramType = Type.getType(parameter);
        if (stringBuilderType.equals(paramType)) {
            logger.info("Converting taint-aware StringBuilder to StringBuilder in multi param method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.stringConfig.getTStringBuilderQN(), "getBuilder", String.format("()%s", Constants.StringBuilderDesc), false);
        }
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Type.getObjectType(owner).equals(stringBuilderType)) {
            String newDescriptor = InstrumentationHelper.getInstance(this.stringConfig).instrumentDesc(descriptor);
            String newOwner = this.stringConfig.getTStringBuilderQN();
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(name, name);

            logger.info("Rewriting StringBuilder invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
            this.mv.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            return true;
        }
        return false;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        Type returnType = Type.getReturnType(desc.toDescriptor());
        if (stringBuilderType.equals(returnType)) {
            logger.info("Converting returned StringBuilder of {}.{}{}", owner, name, desc.toDescriptor());
            this.stringBuilderToTStringBuilder();
        }
    }

    @Override
    public boolean handleLdc(Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        if (stringBuilderType.equals(type)) {
            this.mv.visitLdcInsn(Type.getObjectType(this.stringConfig.getTStringBuilderQN()));
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
        if (Type.getObjectType(type).equals(stringBuilderType) || (isArray && type.endsWith(Constants.StringBuilderDesc))) {
            return this.instrumentQN(type);
        }
        return type;
    }
}
