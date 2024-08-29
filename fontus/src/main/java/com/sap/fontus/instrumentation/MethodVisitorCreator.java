package com.sap.fontus.instrumentation;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface MethodVisitorCreator {
    MethodVisitor create(int opcode, String name, String descriptor, String signature, String[] exceptions);
}
