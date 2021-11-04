package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.AsmPrimitiveBoxer;
import com.sap.fontus.instrumentation.MethodTaintingUtils;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

public abstract class SourceOrSinkTransformer {

    private final int usedLocalVars;

    public SourceOrSinkTransformer(int usedLocalVars) {
        this.usedLocalVars = usedLocalVars;
    }

    protected void pushParameterArrayOntoStack(MethodTaintingVisitor visitor, Descriptor desc) {
        // Array length
        // Stack: number
        MethodTaintingUtils.pushNumberOnTheStack(visitor, desc.parameterCount());
        // Create single dimension array of objects
        // Stack: number --> array reference
        visitor.visitMultiANewArrayInsn(Constants.ObjectQN, 1);

        // Now set array values
        List<String> paramList = desc.getParameters();
        int n = this.usedLocalVars + desc.getParameterTotalSize();
        // Loop over parameters
        for (int i = 0; i < paramList.size(); i++) {
            String p = paramList.get(i);
            Type t = Type.getType(p);
            // Offset of parameter in local variables
            n -= t.getSize();
            // Duplicate array
            // Stack: array --> array, array
            visitor.visitInsn(Opcodes.DUP);
            // Add index to stack
            // Stack: array, array --> array, array, index
            MethodTaintingUtils.pushNumberOnTheStack(visitor, i);
            // Get parameter from local variables
            // Stack: array, array, index --> array, array, index, variable
            visitor.visitVarInsn(t.getOpcode(Opcodes.ILOAD), n);
            // If needed, box primitive values
            // Stack: array, array, index, variable --> array, array, index, (boxed) variable
            AsmPrimitiveBoxer.visitBoxPrimitive(visitor, t);
            // Check cast
            visitor.visitTypeInsn(Opcodes.CHECKCAST, Constants.ObjectQN);
            // Add value to the array
            // Stack: array, array, index, (boxed) variable --> array
            visitor.visitInsn(Opcodes.AASTORE);
        }
    }

    protected void addParentObjectToStack(MethodVisitor mv, FunctionCall func) {
        int size = func.getParsedDescriptor().getParameterTotalSize();
        if (func.isInstanceMethod()) {
            mv.visitVarInsn(Type.getObjectType(func.getOwner()).getOpcode(Opcodes.ILOAD), size + this.usedLocalVars);
        } else {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
    }
}
