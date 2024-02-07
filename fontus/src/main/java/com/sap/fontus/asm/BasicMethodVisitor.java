package com.sap.fontus.asm;

import org.objectweb.asm.MethodVisitor;

public class BasicMethodVisitor extends MethodVisitor {

    private final MethodVisitor parent;

    protected BasicMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
        this.parent = methodVisitor;
    }

    public final MethodVisitor getParentVisitor() {
        return new ParentVisitor();
    }

    public final MethodVisitor getParent() {
        return this.parent;
    }

    /**
     * A MethodVisitor referencing writing through to the visitor passed to the constructor.
     * <p>
     * Allows to factor out calls to the MethodVisitor that shouldn't be transformed further to avoid infinite recursion.
     */
    private class ParentVisitor extends MethodVisitor {
        ParentVisitor() {
            super(BasicMethodVisitor.this.api, BasicMethodVisitor.this.getParent());
        }
    }
}
