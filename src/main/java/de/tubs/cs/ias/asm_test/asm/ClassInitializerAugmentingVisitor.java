package de.tubs.cs.ias.asm_test.asm;

import de.tubs.cs.ias.asm_test.utils.LogUtils;
import de.tubs.cs.ias.asm_test.utils.Utils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import de.tubs.cs.ias.asm_test.utils.Logger;

public class ClassInitializerAugmentingVisitor extends MethodVisitor {
    private static final Logger logger = LogUtils.getLogger();

    private final Collection<FieldData> staticFinalFields;
    private final String owner;

    public ClassInitializerAugmentingVisitor(MethodVisitor methodVisitor,
                                             String owner,
                                             Collection<FieldData> staticFinalFields) {
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
