package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

public class ClassInitializerAugmentingVisitor extends MethodTaintingVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Collection<Tuple<Tuple<String, String>, Object>> staticFinalFields;
    private final String owner;

    ClassInitializerAugmentingVisitor(int acc,
                                      String name,
                                      String methodDescriptor,
                                      MethodVisitor methodVisitor,
                                      String owner,
                                      Collection<Tuple<Tuple<String, String>, Object>> staticFinalFields) {
        super(acc, name, methodDescriptor, methodVisitor);
        this.staticFinalFields = staticFinalFields;
        this.owner = owner;
    }

    @Override
    public void visitInsn(int opcode) {
        if(opcode == Opcodes.RETURN) {
            Utils.writeToStaticInitializer(this, this.owner, this.staticFinalFields);
        }
        super.visitInsn(opcode);
    }
}
