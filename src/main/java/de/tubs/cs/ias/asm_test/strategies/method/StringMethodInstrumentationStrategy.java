package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.JdkClassesLookupTable;
import de.tubs.cs.ias.asm_test.Utils;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintMethod;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.strategies.StringInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;

public class StringMethodInstrumentationStrategy extends StringInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final JdkClassesLookupTable lookupTable = JdkClassesLookupTable.instance;
    private final HashMap<String, String> methodsToRename = new HashMap<>(1);
    private static final Type stringType = Type.getType(String.class);
    private static final Type stringArrayType = Type.getType(String[].class);

    public StringMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(configuration);
        this.mv = mv;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);

    }

    /**
     * One can load String constants directly from the constant pool via the LDC instruction.
     *
     * @param value The String value to load from the constant pool
     */
    private void handleLdcString(Object value) {
        logger.info("Rewriting String LDC to IASString LDC instruction");
        this.mv.visitTypeInsn(Opcodes.NEW, stringConfig.getTStringQN());
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitLdcInsn(value);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, stringConfig.getTStringQN(), Constants.Init, Constants.TStringInitUntaintedDesc, false);
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
        this.mv.visitTypeInsn(Opcodes.NEW, stringConfig.getTStringQN());
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, stringConfig.getTStringQN(), Constants.Init, Constants.TStringInitUntaintedDesc, false);
    }

    private void stringToTStringBuilderBased() {
        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, stringConfig.getTStringQN(), Constants.FROM_STRING, stringConfig.getFROM_STRING_DESC(), false);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        String newOwner = owner;
        if (Constants.StringQN.equals(owner)) {
            newOwner = stringConfig.getTStringQN();
        }
        Matcher matcher = Constants.strPattern.matcher(descriptor);
        if (matcher.find()) {
            if (lookupTable.isJdkClass(newOwner)) {
                this.mv.visitFieldInsn(opcode, newOwner, name, descriptor);
                this.stringToTStringBuilderBased();
            } else {
                String newDescriptor = matcher.replaceAll(stringConfig.getTStringDesc());
                this.mv.visitFieldInsn(opcode, newOwner, name, newDescriptor);
            }
            return true;
        }
        if (!owner.equals(newOwner)) {
            this.mv.visitFieldInsn(opcode, newOwner, name, descriptor);
            return true;
        }
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
        Type paramType = Type.getType(parameter);
        if (stringArrayType.equals(paramType)) {
            logger.info("Converting taint-aware String-Array to String-Array in JDK method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, stringConfig.getTStringUtilsQN(), "convertTaintAwareStringArray", String.format("(%s)%s", stringConfig.getTStringArrayDesc(), Constants.StringArrayDesc), false);
        }
        if (stringType.equals(paramType)) {
            logger.info("Converting taint-aware String to String in JDK method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, stringConfig.getTStringQN(), Constants.AS_STRING, stringConfig.getAS_STRING_DESC(), false);
        }
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Type.getObjectType(owner).equals(stringType) || owner.endsWith(Constants.StringDesc)) {
            String newDescriptor = InstrumentationHelper.getInstance(this.stringConfig).instrumentDesc(descriptor);
            String newOwner = owner.replace(Constants.StringQN, stringConfig.getTStringQN());
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
        Type returnType = Type.getReturnType(desc.toDescriptor());
        if (stringType.equals(returnType)) {
            this.stringToTStringBuilderBased();
            logger.info("Converting returned String of {}.{}{}", owner, name, desc.toDescriptor());
        } else if (stringArrayType.equals(returnType)) {
            logger.info("Converting returned String Array of {}.{}{}", owner, name, desc.toDescriptor());
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, stringConfig.getTStringUtilsQN(), "convertStringArray", String.format("(%s)%s", Constants.StringArrayDesc, stringConfig.getTStringArrayDesc()), false);
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
        if (stringType.equals(type)) {
            this.mv.visitLdcInsn(Type.getObjectType(stringConfig.getTStringQN()));
            return true;
        }
        return false;
    }

    @Override
    public boolean handleLdcArray(Type type) {
        Type stringArray = Type.getType(String[].class);
        if (stringArray.equals(type)) {
            Type taintStringArray = Type.getType(this.stringConfig.getTStringArrayDesc());
            this.mv.visitLdcInsn(taintStringArray);
            return true;
        }
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        boolean isArray = type.startsWith("[");
        if (Type.getObjectType(type).equals(stringType) || (isArray && type.endsWith(Constants.StringDesc))) {
            return this.instrumentQN(type);
        }
        return type;
    }

}
