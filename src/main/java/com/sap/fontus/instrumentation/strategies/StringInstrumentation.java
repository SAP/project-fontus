package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.TriConsumer;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import org.objectweb.asm.*;

import java.util.Optional;
import java.util.regex.Matcher;

public class StringInstrumentation extends AbstractInstrumentation {
    private final TaintStringConfig taintStringConfig;
    public StringInstrumentation(TaintStringConfig taintStringConfig, InstrumentationHelper instrumentationHelper) {
        super(Type.getType(String.class), Type.getObjectType(taintStringConfig.getTStringQN()), instrumentationHelper, Constants.TStringToStringName);
        this.taintStringConfig = taintStringConfig;
    }

    @Override
    public String instrumentDescForIASCall(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.strPattern.matcher(parameters).replaceAll(this.taintStringConfig.getMethodTStringDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.strPattern.matcher(returnType).replaceAll(this.taintStringConfig.getTStringDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }

    @Override
    public boolean handleLdc(MethodVisitor mv, Object value) {
        // When loading a constant, make a taint-aware string out of a string constant.
        if (value instanceof String) {
            this.handleLdcString(mv, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleLdcArray(MethodVisitor mv, Type type) {
        Type stringArray = Type.getType(String[].class);
        if (stringArray.equals(type)) {
            Type taintStringArray = this.getInstrumentedArrayDescriptorType();
            mv.visitLdcInsn(taintStringArray);
            return true;
        }
        return false;
    }

    /**
     * One can load String constants directly from the constant pool via the LDC instruction.
     *
     * @param value The String value to load from the constant pool
     */
    private void handleLdcString(MethodVisitor mv, Object value) {
        logger.info("Rewriting String LDC to IASString LDC instruction");
        mv.visitTypeInsn(Opcodes.NEW, this.instrumentedType.getInternalName());
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(value);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, this.instrumentedType.getInternalName(), Constants.Init, Constants.TStringInitUntaintedDesc, false);
        // Interning not necessary, because CompareProxy catches comparings
        // Maybe increases memory footprint enormous
//        this.mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.stringConfig.getTStringQN(), "intern", String.format("()%s", Type.getType(IASStringable.class).getDescriptor()), false);
        mv.visitTypeInsn(Opcodes.CHECKCAST, this.instrumentedType.getInternalName());
    }

    @Override
    public Optional<FieldVisitor> instrumentFieldInstruction(ClassVisitor classVisitor, int access, String name, String descriptor, String signature, Object value, TriConsumer tc) {
        Matcher descMatcher = this.descPattern.matcher(descriptor);
        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(this.instrumentedType.getDescriptor());
            logger.info("Replacing String field [{}]{}.{} with [{}]{}.{}", access, name, descriptor, access, name, newDescriptor);
            if (value != null && (access & Opcodes.ACC_FINAL) != 0 && (access & Opcodes.ACC_STATIC) != 0) {
                tc.apply(name, descriptor, value);
            }
            return Optional.of(classVisitor.visitField(access, name, newDescriptor, signature, null));
        }
        return Optional.empty();
    }
}
