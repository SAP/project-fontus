package com.sap.fontus.instrumentation.compat;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.CompatHelper;
import com.sap.fontus.instrumentation.MethodVisitorCreator;
import com.sap.fontus.taintaware.unified.IASString;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SqliteDB implements CompatHelper.CompatImplementation {

    private final String affects = "org/sqlite/core/DB";

    @Override
    public String getAffects() {
        return this.affects;
    }

    @Override
    public void apply(String owner, MethodVisitorCreator methodVisitorCreator) {
        assert this.affects.equals(owner);
        this.createOnUpdate(methodVisitorCreator, owner);
    }

    private void createOnUpdate(MethodVisitorCreator methodVisitorCreator, String owner) {
        MethodVisitor mv = methodVisitorCreator.create(Opcodes.ACC_PUBLIC, "onUpdate", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.getType(String.class), Type.getType(String.class), Type.LONG_TYPE), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IASString.class), Constants.FROM_STRING, Constants.FROM_STRING_DESCRIPTOR, false);
        mv.visitVarInsn(Opcodes.LLOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, "onUpdate", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.getType(IASString.class), Type.getType(IASString.class), Type.LONG_TYPE), false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();
    }
}
