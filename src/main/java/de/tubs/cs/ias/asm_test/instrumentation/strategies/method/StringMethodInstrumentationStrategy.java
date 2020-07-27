package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.JdkClassesLookupTable;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.StringInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.regex.Matcher;

public class StringMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    private static final Type stringArrayType = Type.getType(String[].class);

    public StringMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(mv, configuration.getTStringDesc(), configuration.getTStringQN(), Constants.TStringToStringName, String.class, configuration, new StringInstrumentation(configuration));

    }

    /**
     * One can load String constants directly from the constant pool via the LDC instruction.
     *
     * @param value The String value to load from the constant pool
     */
    private void handleLdcString(Object value) {
        logger.info("Rewriting String LDC to IASString LDC instruction");
        this.mv.visitTypeInsn(Opcodes.NEW, this.stringConfig.getTStringQN());
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitLdcInsn(value);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.stringConfig.getTStringQN(), Constants.Init, Constants.TStringInitUntaintedDesc, false);
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
        this.mv.visitTypeInsn(Opcodes.NEW, this.stringConfig.getTStringQN());
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.stringConfig.getTStringQN(), Constants.Init, Constants.TStringInitUntaintedDesc, false);
    }

    private void stringToTStringBuilderBased() {
        this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.stringConfig.getTStringQN(), Constants.FROM_STRING, this.stringConfig.getFromStringDesc(), false);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        String newOwner = owner;
        if (Constants.StringQN.equals(owner)) {
            newOwner = this.stringConfig.getTStringQN();
        }
        Matcher matcher = Constants.strPattern.matcher(descriptor);
        if (matcher.find()) {
            if (JdkClassesLookupTable.getInstance().isJdkClass(newOwner)) {
                this.mv.visitFieldInsn(opcode, newOwner, name, descriptor);
                this.stringToTStringBuilderBased();
            } else {
                String newDescriptor = matcher.replaceAll(this.stringConfig.getTStringDesc());
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
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.stringConfig.getSharedTStringUtilsQN(), "convertTaintAwareStringArray", String.format("([%s)%s", this.stringConfig.getMethodTStringDesc(), Constants.StringArrayDesc), false);
        }
        if (this.type.equals(paramType)) {
            logger.info("Converting taint-aware String to String in JDK method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.stringConfig.getTStringQN(), Constants.AS_STRING, this.stringConfig.getAsStringDesc(), false);
        }
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Type.getObjectType(owner).equals(this.type) || owner.endsWith(Constants.StringDesc)) {
            String newDescriptor = InstrumentationHelper.getInstance(this.stringConfig).instrumentDesc(descriptor);
            String newOwner = this.qnPattern.matcher(owner).replaceAll(Matcher.quoteReplacement(this.stringConfig.getTStringQN()));
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
        if (this.type.equals(returnType)) {
            this.stringToTStringBuilderBased();
            logger.info("Converting returned String of {}.{}{}", owner, name, desc.toDescriptor());
        } else if (stringArrayType.equals(returnType)) {
            logger.info("Converting returned String Array of {}.{}{}", owner, name, desc.toDescriptor());
            this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, this.stringConfig.getTStringUtilsQN(), "convertStringArray", String.format("([L%s;)%s", Utils.fixupReverse(String.class.getName()), this.stringConfig.getTStringArrayDesc()), false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getType(this.stringConfig.getTStringArrayDesc()).getInternalName());
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
    public boolean handleLdcArray(Type type) {
        Type stringArray = Type.getType(String[].class);
        if (stringArray.equals(type)) {
            Type taintStringArray = Type.getType(this.stringConfig.getTStringArrayDesc());
            this.mv.visitLdcInsn(taintStringArray);
            return true;
        }
        return false;
    }
}
