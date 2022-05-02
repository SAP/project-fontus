package com.sap.fontus.asm;

import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.Logger;
import com.sap.fontus.utils.Utils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collection;

public class ClassInitializerAugmentingVisitor extends MethodVisitor {
    private static final Logger logger = LogUtils.getLogger();

    private final Collection<FieldData> staticFinalFields;
    private final String owner;

    public ClassInitializerAugmentingVisitor(MethodVisitor methodVisitor,
                                             String owner,
                                             Collection<FieldData> staticFinalFields) {
        super(Opcodes.ASM9, methodVisitor);
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
