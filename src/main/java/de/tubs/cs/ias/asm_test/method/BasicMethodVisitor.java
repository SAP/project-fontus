package de.tubs.cs.ias.asm_test.method;


import org.objectweb.asm.*;

public class BasicMethodVisitor extends MethodVisitor {

    private final MethodVisitor parent;

    public BasicMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
        this.parent = methodVisitor;
    }

    public MethodVisitor getParentVisitor() {
        return new ParentVisitor();
    }

    public MethodVisitor getParent() {
        return this.parent;
    }

    /**
     * A MethodVisitor referencing writing through to the visitor passed to the constructor.
     *
     * Allows to factor out calls to the MethodVisitor that shouldn't be transformed further to avoid infinite recursion.
     */
    private class ParentVisitor extends MethodVisitor {
        ParentVisitor() {
            super(BasicMethodVisitor.this.api, BasicMethodVisitor.this.getParent());
        }
    }
}
