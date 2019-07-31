package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;

public class ClassInitializerAugmentingVisitor extends MethodVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Collection<Tuple<Tuple<String, String>, Object>> staticFinalFields;
    private final String owner;

    ClassInitializerAugmentingVisitor(MethodVisitor methodVisitor,
                                      String owner,
                                      Collection<Tuple<Tuple<String, String>, Object>> staticFinalFields) {
        super(Opcodes.ASM7, methodVisitor);
        this.staticFinalFields = new ArrayList<>(staticFinalFields.size());
        this.staticFinalFields.addAll(staticFinalFields);
        this.owner = owner;
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            Utils.writeToStaticInitializer(this, this.owner, this.staticFinalFields);
        }
        super.visitInsn(opcode);
    }
}
