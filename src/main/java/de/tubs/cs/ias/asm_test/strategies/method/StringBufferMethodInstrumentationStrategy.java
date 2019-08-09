package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;
import de.tubs.cs.ias.asm_test.strategies.StringBufferInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBufferMethodInstrumentationStrategy extends StringBufferInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final HashMap<String, String> methodsToRename = new HashMap<>(1);
    private static final Pattern STRING_BUFFER_QN_PATTERN = Pattern.compile(Constants.StringBufferQN, Pattern.LITERAL);

    public StringBufferMethodInstrumentationStrategy(MethodVisitor mv) {
        this.mv = mv;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    private void stringBufferToTStringBuffer() {
        this.mv.visitTypeInsn(Opcodes.NEW, Constants.TStringBufferQN);
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringBufferQN, Constants.Init, String.format("(%s)V", Constants.StringBufferDesc), false);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = Constants.strBufferPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(Constants.TStringBufferDesc);
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
        if (Constants.StringBufferDesc.equals(parameter)) {
            logger.info("Converting taint-aware StringBuffer to StringBuffer in multi param method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringBufferQN, "getBuffer", String.format("()%s", Constants.StringBufferDesc), false);
        }
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        if(desc.getReturnType().equals(Constants.StringBufferDesc)) {
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
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        boolean isArray = type.startsWith("[");
        if (type.equals(Constants.StringBufferQN) || (isArray && type.endsWith(Constants.StringBufferDesc))) {
            return STRING_BUFFER_QN_PATTERN.matcher(type).replaceAll(Matcher.quoteReplacement(Constants.TStringBufferQN));
        }
        return type;
    }


    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if(owner.equals(Constants.StringBufferQN)) {
            Matcher sbDescMatcher = Constants.strBufferPattern.matcher(descriptor);
            String newOwner = Constants.TStringBufferQN;
            String newDescriptor = sbDescMatcher.replaceAll(Constants.TStringBufferDesc);
            // Replace all instances of java/lang/String
            Matcher newDescriptorMatcher = Constants.strPattern.matcher(newDescriptor);
            String finalDescriptor = newDescriptorMatcher.replaceAll(Constants.TStringDesc);
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(name, name);

            logger.info("Rewriting StringBuffer invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, finalDescriptor);
            this.mv.visitMethodInsn(opcode, newOwner, newName, finalDescriptor, isInterface);
            return true;
        }
        return false;
    }
}
