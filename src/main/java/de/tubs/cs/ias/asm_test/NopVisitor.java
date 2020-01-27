package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.*;

public class NopVisitor extends ClassVisitor {

    public NopVisitor(int flags) {
        super(flags);
    }

    public NopVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

}

