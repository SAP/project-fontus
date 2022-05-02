package com.sap.fontus.asm;

import org.objectweb.asm.ClassVisitor;

public class NopVisitor extends ClassVisitor {

    public NopVisitor(int flags) {
        super(flags);
    }

    public NopVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

}

