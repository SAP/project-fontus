package com.sap.fontus.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class AsmPrimitiveBoxer {

    public static void visitBoxPrimitive(MethodVisitor mv, Type type) {
        if (Type.BOOLEAN_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (Type.BYTE_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (Type.CHAR_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        } else if (Type.SHORT_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (Type.INT_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (Type.FLOAT_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (Type.LONG_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        } else if (Type.DOUBLE_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        }
    }
}
