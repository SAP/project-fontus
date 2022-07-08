package com.sap.fontus.instrumentation;

import com.sap.fontus.Constants;
import com.sap.fontus.taintaware.unified.IASString;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Helper methods for MethodTaintingVisitor and ClassTaintingVisitor
 */
public final class TaintingUtils {

    private TaintingUtils() {

    }

    /**
     * Converts a potentially untainted type on the stack to its untainted version.
     *
     * We try to insert "correct" conversion calls instead of calling the generic ConversionUtils here.
     *
     * @param sourceType The target type
     * @param targetType The target type
     * @param mv The MethodVisitor where the conversion call shall be inserted
     */
    public static void convertTypeToUntainted(String sourceType, String targetType, MethodVisitor mv) {
        Type target = Type.getType(targetType);
        Type source = Type.getType(sourceType);
        convertTypeToUntainted(source, target, mv);
    }

    /**
     * Converts a potentially tainted type on the stack to its untainted version.
     *
     * We try to insert "correct" conversion calls instead of calling the generic ConversionUtils here.
     *
     * @param source The target type
     * @param target The target type
     * @param mv The MethodVisitor where the conversion call shall be inserted
     */
    public static void convertTypeToUntainted(Type source, Type target, MethodVisitor mv) {
        if (source.equals(Type.getType(IASString.class)) && target.equals(Type.getType(String.class))) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), "toStringNullable", Type.getMethodDescriptor(Type.getType(String.class), Type.getType(IASString.class)), false);
        } else {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToOrigName, Constants.ConversionUtilsToOrigDesc, false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, target.getInternalName());
        }
    }

    /**
     * Converts a potentially untainted type on the stack to its tainted version.
     *
     * We try to insert "correct" conversion calls instead of calling the generic ConversionUtils here.
     *
     * @param source The target type
     * @param target The target type
     * @param mv The MethodVisitor where the conversion call shall be inserted
     */
    public static void convertTypeToTainted(Type source, Type target, MethodVisitor mv) {
        if (target.equals(Type.getType(IASString.class)) && source.equals(Type.getType(String.class))) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
        } else {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.ConversionUtilsQN, Constants.ConversionUtilsToConcreteName, Constants.ConversionUtilsToConcreteDesc, false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, target.getInternalName());
        }
    }
}
