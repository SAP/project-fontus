package com.sap.fontus.instrumentation.compat;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.CompatHelper;
import com.sap.fontus.instrumentation.MethodVisitorCreator;
import com.sap.fontus.taintaware.unified.IASString;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SqliteCollation implements CompatHelper.CompatImplementation {

    private final String affects = "org/sqlite/Collation";

    @Override
    public String getAffects() {
        return this.affects;
    }

    @Override
    public void apply(String owner, MethodVisitorCreator methodVisitorCreator) {
        assert this.affects.equals(owner);
        MethodVisitor mv = methodVisitorCreator.create(Opcodes.ACC_PUBLIC, "xCompare", Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(String.class), Type.getType(String.class)), null, null);
        mv.visitCode();
        createSqliteXCompareProxy(mv, owner);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    static void createSqliteXCompareProxy(MethodVisitor mv, String owner) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, "xCompare", Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(IASString.class), Type.getType(IASString.class)), false);
        mv.visitInsn(Opcodes.IRETURN);
    }
}
