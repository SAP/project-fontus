package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.strategies.FormatterInstrumentation;
import de.tubs.cs.ias.asm_test.strategies.InstrumentationHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Formatter;
import java.util.HashMap;
import java.util.regex.Matcher;

public class FormatterMethodInstrumentationStrategy extends FormatterInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    private final HashMap<String, String> methodsToRename = new HashMap<>(1);
    private static final Type formatterType = Type.getType(Formatter.class);
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public FormatterMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig configuration) {
        super(configuration);
        this.mv = parentVisitor;
        this.methodsToRename.put(Constants.ToString, Constants.TO_TSTRING);
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = Constants.formatterPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(this.stringConfig.getTFormatterDesc());
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {
        Type paramType = Type.getType(parameter);
        if (formatterType.equals(paramType)) {
            logger.info("Converting taint-aware Formatter to Formatter in multi param method invocation");
            this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.stringConfig.getTFormatterQN(), Constants.TFormatterToFormatterName, String.format("()%s", this.stringConfig.getTFormatterDesc()), false);
        }
    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (Type.getObjectType(owner).equals(formatterType)) {
            String newDescriptor = InstrumentationHelper.getInstance(this.stringConfig).instrumentDesc(descriptor);
            String newOwner = this.stringConfig.getTFormatterQN();
            // Some methods names (e.g., toString) need to be replaced to not break things, look those up
            String newName = this.methodsToRename.getOrDefault(name, name);

            logger.info("Rewriting Formatter invoke [{}] {}.{}{} to {}.{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, newDescriptor);
            this.mv.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            return true;
        }
        return false;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {
        Type returnType = Type.getReturnType(desc.toDescriptor());
        if (formatterType.equals(returnType)) {
            logger.info("Converting returned Formatter of {}.{}{}", owner, name, desc.toDescriptor());
            this.formatterToTFormatter();
        }
    }

    private void formatterToTFormatter() {
        this.mv.visitTypeInsn(Opcodes.NEW, this.stringConfig.getTFormatterQN());
        this.mv.visitInsn(Opcodes.DUP);
        this.mv.visitInsn(Opcodes.DUP2_X1);
        this.mv.visitInsn(Opcodes.POP2);
        this.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.stringConfig.getTFormatterQN(), Constants.Init, String.format("(%s)V", Constants.FormatterDesc), false);
    }

    @Override
    public boolean handleLdc(Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        if (formatterType.equals(type)) {
            this.mv.visitLdcInsn(Type.getObjectType(this.stringConfig.getTFormatterQN()));
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
        if (Type.getObjectType(type).equals(formatterType) || (isArray && type.endsWith(Constants.FormatterDesc))) {
            return this.instrumentQN(type);
        }
        return type;
    }
}
