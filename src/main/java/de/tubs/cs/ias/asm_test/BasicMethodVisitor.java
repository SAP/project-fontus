package de.tubs.cs.ias.asm_test;


import org.objectweb.asm.MethodVisitor;

class BasicMethodVisitor extends MethodVisitor {

    BasicMethodVisitor(int api) {
        super(api);
    }

    BasicMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    MethodVisitor getParentVisitor() {
        return new ParentVisitor();
    }

    private class ParentVisitor extends MethodVisitor {
        ParentVisitor() {
            super(BasicMethodVisitor.this.api, BasicMethodVisitor.this);
        }
    }
}
