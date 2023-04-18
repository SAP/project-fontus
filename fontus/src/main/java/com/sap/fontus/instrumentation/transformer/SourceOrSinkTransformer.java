package com.sap.fontus.instrumentation.transformer;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.instrumentation.MethodTaintingUtils;
import com.sap.fontus.instrumentation.MethodTaintingVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public abstract class SourceOrSinkTransformer {

    private final int usedLocalVars;
    private final FunctionCall caller;

    public SourceOrSinkTransformer(int usedLocalVars, FunctionCall caller) {
        this.usedLocalVars = usedLocalVars;
        this.caller = caller;
    }

    protected FunctionCall getCaller() {
        return this.caller;
    }

    public static void visitBoxPrimitive(MethodVisitor mv, Type type) {
        if (Type.BOOLEAN_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (Type.BYTE_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (Type.CHAR_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        } else if (Type.SHORT_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (Type.INT_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (Type.FLOAT_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (Type.LONG_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        } else if (Type.DOUBLE_TYPE.equals(type)) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        }
    }

    protected void pushParameterArrayOntoStack(MethodTaintingVisitor visitor, Descriptor desc) {
        this.pushParameterArrayOntoStack(visitor, desc, new ArrayList<>(0));
    }
    protected void pushParameterArrayOntoStack(MethodTaintingVisitor visitor, Descriptor desc, List<Integer> passLocals) {
        // Array length
        // Stack: number
        MethodTaintingUtils.pushNumberOnTheStack(visitor, desc.parameterCount()+passLocals.size());
        // Create single dimension array of objects
        // Stack: number --> array reference
        visitor.visitTypeInsn(Opcodes.ANEWARRAY, Constants.ObjectQN);

        // Now set array values
        List<String> paramList = desc.getParameters();
        int n = this.usedLocalVars + desc.getParameterTotalSize();
        // Loop over parameters
        int i = 0;
        for (; i < paramList.size(); i++) {
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
            visitBoxPrimitive(visitor, t);
            // Check cast
            visitor.visitTypeInsn(Opcodes.CHECKCAST, Constants.ObjectQN);
            // Add value to the array
            // Stack: array, array, index, (boxed) variable --> array
            visitor.visitInsn(Opcodes.AASTORE);
        }
        // Allows to put additional local variables into the array
        // stack: array
        for(Integer j : passLocals) {
            // Duplicate array
            // Stack: array --> array, array
            visitor.visitInsn(Opcodes.DUP);
            // Stack: array, array --> array, array, index
            MethodTaintingUtils.pushNumberOnTheStack(visitor, i);
            // stack: array, array, index -> array, array, index, object
            visitor.visitVarInsn(Opcodes.ALOAD, j);
            // Add value to the array
            // Stack: array, array, index, object --> array
            visitor.visitInsn(Opcodes.AASTORE);
            i++;
        }
    }

    protected void addParentObjectToStack(MethodVisitor mv, FunctionCall func) {
        int size = func.getParsedDescriptor().getParameterTotalSize();
        // If the method is static there is no object to put on the stack
        // Similarly, constructor methods are called before the object is initialized
        if (func.isInstanceMethod() && !func.isConstructor()) {
            mv.visitVarInsn(Type.getObjectType(func.getOwner()).getOpcode(Opcodes.ILOAD), size + this.usedLocalVars);
        } else {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
    }
}
