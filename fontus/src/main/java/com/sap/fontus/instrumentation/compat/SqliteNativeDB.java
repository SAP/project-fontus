package com.sap.fontus.instrumentation.compat;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.CompatHelper;
import com.sap.fontus.instrumentation.MethodVisitorCreator;
import com.sap.fontus.taintaware.unified.IASString;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SqliteNativeDB implements CompatHelper.CompatImplementation {

    private final String affects = "org/sqlite/core/NativeDB";

    @Override
    public String getAffects() {
        return this.affects;
    }

    @Override
    public void apply(String owner, MethodVisitorCreator methodVisitorCreator) {
        assert this.affects.equals(owner);
        createSqliteXThrowEx(methodVisitorCreator, owner);
        createSqliteStringToUtf8ByteArray(methodVisitorCreator, owner);
    }

    static void createSqliteStringToUtf8ByteArray(MethodVisitorCreator methodVisitorCreator, String owner) {
        MethodVisitor mv = methodVisitorCreator.create(Opcodes.ACC_STATIC, "stringToUtf8ByteArray", Type.getMethodDescriptor(Type.getType("[B"), Type.getType(String.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, "stringToUtf8ByteArray", Type.getMethodDescriptor(Type.getType("[B"), Type.getType(IASString.class)), false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

        static void createSqliteXThrowEx(MethodVisitorCreator methodVisitorCreator, String owner) {
        MethodVisitor mv = methodVisitorCreator.create(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "throwex", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, "throwex", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(IASString.class)), false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

    }
}
