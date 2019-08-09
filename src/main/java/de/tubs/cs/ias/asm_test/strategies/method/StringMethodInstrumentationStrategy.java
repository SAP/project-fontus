package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.Utils;
import de.tubs.cs.ias.asm_test.strategies.StringInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringMethodInstrumentationStrategy extends StringInstrumentation implements MethodInstrumentationStrategy{
    private final MethodVisitor mv;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final HashMap<String, String> methodsToRename = new HashMap<>(1);
    private static final Pattern STRING_QN_PATTERN = Pattern.compile(Constants.StringQN, Pattern.LITERAL);

    public StringMethodInstrumentationStrategy(MethodVisitor mv) {
        this.mv = mv;
        this.methodsToRename.put(Constants.ToString, "toIASString");

    }

    /**
     * One can load String constants directly from the constant pool via the LDC instruction.
     *
     * @param value The String value to load from the constant pool
     */
    private void handleLdcString(Object value) {
        logger.info("Rewriting String LDC to IASString LDC instruction");
        this.mv.visitTypeInsn(Opcodes.NEW, Constants.TStringQN);
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitLdcInsn(value);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringQN, Constants.Init, Constants.TStringInitUntaintedDesc, false);
    }

    /**
     * Converts a String that's top of the stack to an taint-aware String
     * Precondition: String instance that's on top of the Stack!!
     */
    private void stringToTString() {
        /*
        Operand stack:
        +-------+ new  +----------+ dup  +----------+ dup2_x1  +----------+  pop2  +----------+ ispecial  +----------+
        |String +----->+IASString +----->+IASString +--------->+IASString +------->+String    +---------->+IASString |
        +-------+      +----------+      +----------+          +----------+        +----------+ init      +----------+
                       +----------+      +----------+          +----------+        +----------+
                       |String    |      |IASString |          |IASString |        |IASString |
                       +----------+      +----------+          +----------+        +----------+
                                         +----------+          +----------+        +----------+
                                         |String    |          |String    |        |IASString |
                                         +----------+          +----------+        +----------+
                                                               +----------+
                                                               |IASString |
                                                               +----------+
                                                               +----------+
                                                               |IASString |
                                                               +----------+
        */
        this.mv.visitTypeInsn(Opcodes.NEW, Constants.TStringQN);
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringQN, Constants.Init, Constants.TStringInitUntaintedDesc, false);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = Constants.strPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(Constants.TStringDesc);
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
        if (Constants.StringArrayDesc.equals(parameter)) {
            logger.info("Converting taint-aware String-Array to String-Array in multi param method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringQN, "convertTaintAwareStringArray", String.format("(%s)%s", Constants.TStringArrayDesc, Constants.StringArrayDesc), false);
        }
        if (Constants.StringDesc.equals(parameter)) {
            logger.info("Converting taint-aware String to String in multi param method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
        }
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (owner.equals(Constants.StringQN)) {
            Matcher stringDescMatcher = Constants.strPattern.matcher(descriptor);
            String newOwner = Constants.TStringQN;
            String newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
            // TODO: this call is superfluous, TString.toTString is a NOP pretty much.. Maybe drop those calls?
            String newName = this.methodsToRename.getOrDefault(name, name);
            logger.info("Rewriting String invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
            this.mv.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            return true;
        }
        return false;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        if (Constants.StringDesc.equals(desc.getReturnType())) {
            this.stringToTString();
            logger.info("Converting returned String of {}.{}{}", owner, name, desc.toDescriptor());
        } else if (Constants.StringArrayDesc.equals(desc.getReturnType())) {
            logger.info("Converting returned String Array of {}.{}{}", owner, name, desc.toDescriptor());
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TStringUtilsQN, "convertStringArray", String.format("(%s)%s", Constants.StringArrayDesc, Constants.TStringArrayDesc), false);
        }
    }

    @Override
    public boolean handleLdc(Object value) {
        // When loading a constant, make a taint-aware string out of a string constant.
        if (value instanceof String) {
            this.handleLdcString(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        if ("java.lang.String".equals(type.getClassName())) {
            this.mv.visitLdcInsn(Type.getObjectType(Constants.TStringQN));
            return true;
        }
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        boolean isArray = type.startsWith("[");
        if (type.equals(Constants.StringQN) || (isArray && type.endsWith(Constants.StringDesc))) {
            return STRING_QN_PATTERN.matcher(type).replaceAll(Matcher.quoteReplacement(Constants.TStringQN));
        }
        return type;
    }

}
