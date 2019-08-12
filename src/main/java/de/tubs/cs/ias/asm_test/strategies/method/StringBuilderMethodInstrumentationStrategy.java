package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;
import de.tubs.cs.ias.asm_test.strategies.StringBuilderInstrumentation;
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

    public StringBuilderMethodInstrumentationStrategy(MethodVisitor mv) {
        this.mv = mv;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    private void stringBuilderToTStringBuilder() {
        this.mv.visitTypeInsn(Opcodes.NEW, Constants.TStringBuilderQN);
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringBuilderQN, Constants.Init, String.format("(%s)V", Constants.StringBuilderDesc), false);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = Constants.strBuilderPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(Constants.TStringBuilderDesc);
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
        if (Constants.StringBuilderDesc.equals(parameter)) {
            logger.info("Converting taint-aware StringBuilder to StringBuilder in multi param method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringBuilderQN, "getBuilder", String.format("()%s", Constants.StringBuilderDesc), false);
        }
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if(owner.equals(Constants.StringBuilderQN)) {
            Matcher sbDescMatcher = Constants.strBuilderPattern.matcher(descriptor);
            String newOwner = Constants.TStringBuilderQN;
            String newDescriptor = sbDescMatcher.replaceAll(Constants.TStringBuilderDesc);
            // Replace all instances of java/lang/String
            Matcher newDescriptorMatcher = Constants.strPattern.matcher(newDescriptor);
            String finalDescriptor = newDescriptorMatcher.replaceAll(Constants.TStringDesc);
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(name, name);

            logger.info("Rewriting StringBuilder invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, finalDescriptor);
            this.mv.visitMethodInsn(opcode, newOwner, newName, finalDescriptor, isInterface);
            return true;
        }
        return false;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        if(desc.getReturnType().equals(Constants.StringBuilderDesc)) {
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
        if ("java.lang.StringBuilder".equals(type.getClassName())) {
            this.mv.visitLdcInsn(Type.getObjectType(Constants.TStringBuilderQN));
            return true;
        }
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        boolean isArray = type.startsWith("[");
        if (type.equals(Constants.StringBuilderQN) || (isArray && type.endsWith(Constants.StringBuilderDesc))) {
            return this.instrumentQN(type);
        }
       return type;
    }
}
