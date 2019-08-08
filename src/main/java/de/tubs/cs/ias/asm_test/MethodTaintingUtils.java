package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class MethodTaintingUtils {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Converts a String that's top of the stack to an taint-aware String
     * Precondition: String instance that's on top of the Stack!!
     */
    public static void stringToTString(MethodVisitor mv) {
        /*
        Operand stack:
        +-------+ new  +----------+ dup  +----------+ dup2_x1  +----------+  pop2  +----------+ ispecial  +----------+
        |String +----->+IASString +----->+IASString +--------->+IASString +------->+String    +---------->+IASString |
        +-------+      +----------+      +----------+          +----------+        +----------+ init      +----------+
                       +----------+      +----------+          +----------+        +----------+
                       |String    |      |IASString |          |IASString |        |IASString |
                       +----------+      +----------+          +----------+        +----------+
                                         +----------+          +----------+        +----------+
                                         |String    |          |String    |        |IASString |
                                         +----------+          +----------+        +----------+
                                                               +----------+
                                                               |IASString |
                                                               +----------+
                                                               +----------+
                                                               |IASString |
                                                               +----------+
        */
        mv.visitTypeInsn(Opcodes.NEW, Constants.TStringQN);
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.DUP2_X1);
        mv.visitInsn(Opcodes.POP2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringQN, Constants.Init, Constants.TStringInitUntaintedDesc, false);
    }

    public static void stringBufferToTStringBuffer(MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.NEW, Constants.TStringBufferQN);
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.DUP2_X1);
        mv.visitInsn(Opcodes.POP2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringBufferQN, Constants.Init, String.format("(%s)V", Constants.StringBufferDesc), false);
    }

    public static void stringBuilderToTStringBuilder(MethodVisitor mv) {
        mv.visitTypeInsn(Opcodes.NEW, Constants.TStringBuilderQN);
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.DUP2_X1);
        mv.visitInsn(Opcodes.POP2);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringBuilderQN, Constants.Init, String.format("(%s)V", Constants.StringBuilderDesc), false);
    }

    /**
     * If a taint-aware string is on the top of the stack, we can call this function to add a check to handle tainted strings.
     */
    static void callCheckTaint(MethodVisitor mv) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.ABORT_IF_TAINTED, "()V", false);
    }

    /**
     * Pushes an integer onto the stack.
     * Optimizes small integers towards their dedicated ICONST_n instructions to save space.
     */
    static void pushNumberOnTheStack(MethodVisitor mv, int num) {
        switch (num) {
            case -1:
                mv.visitInsn(Opcodes.ICONST_M1);
                return;
            case 0:
                mv.visitInsn(Opcodes.ICONST_0);
                return;
            case 1:
                mv.visitInsn(Opcodes.ICONST_1);
                return;
            case 2:
                mv.visitInsn(Opcodes.ICONST_2);
                return;
            case 3:
                mv.visitInsn(Opcodes.ICONST_3);
                return;
            case 4:
                mv.visitInsn(Opcodes.ICONST_4);
                return;
            case 5:
                mv.visitInsn(Opcodes.ICONST_5);
                return;
            default:
                mv.visitIntInsn(Opcodes.BIPUSH, num);
        }
    }

    /**
     * Converts a primitive type to its boxed type.
     *
     * @param type The type to potential convert.
     */
    static void invokeConversionFunction(MethodVisitor mv, String type) {
        Map<String, String> types = new HashMap<>();
        types.put("I", "Integer");
        types.put("B", "Byte");
        types.put("C", "Character");
        types.put("D", "Double");
        types.put("F", "Float");
        types.put("J", "Long");
        types.put("S", "Short");
        types.put("Z", "Boolean");

        // No primitive type, nop
        if(!types.containsKey(type)) return;

        String full = types.get(type);
        String owner = String.format("java/lang/%s", full);
        String desc = String.format("(%s)L%s;", type, owner);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, "valueOf", desc, false);
    }

    /**
     * Translates the call to a lambda function
     */
    static void invokeVisitLambdaCall(MethodVisitor mv,
                                       final String name,
                                       final String descriptor,
                                       final Handle bootstrapMethodHandle,
                                       final Object... bootstrapMethodArguments) {
        Object[] bsArgs = new Object[bootstrapMethodArguments.length];
        for (int i = 0; i < bootstrapMethodArguments.length; i++) {
            Object arg = bootstrapMethodArguments[i];
            if (arg instanceof Handle) {
                Handle a = (Handle) arg;
                bsArgs[i] = Utils.instrumentHandle(a);
            } else if (arg instanceof Type) {
                Type a = (Type) arg;
                bsArgs[i] = Utils.instrumentType(a);
            } else {
                bsArgs[i] = arg;
            }
        }
        String desc = Utils.rewriteDescriptor(descriptor);
        mv.visitInvokeDynamicInsn(name, desc, bootstrapMethodHandle, bsArgs);
    }

    /**
     * Does potential String -> TString transformations for a single parameter JDK function
     *
     * @param desc The method's descriptor.
     */
    static void handleSingleParameterJdkMethod(MethodVisitor mv, Descriptor desc) {
        if(!desc.hasStringLikeParameters()) return;

        String param = desc.getParameterStack().pop();
        if ((Constants.StringDesc).equals(param)) {
            logger.info("Converting taint-aware String to String in single param method invocation");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TStringQN, Constants.TStringToStringName, Constants.ToStringDesc, false);
        }
    }

    /**
     * One can load String constants directly from the constant pool via the LDC instruction.
     *
     * @param value The String value to load from the constant pool
     */
    static void handleLdcString(MethodVisitor mv, Object value) {
        logger.info("Rewriting String LDC to IASString LDC instruction");
        mv.visitTypeInsn(Opcodes.NEW, Constants.TStringQN);
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(value);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TStringQN, Constants.Init, Constants.TStringInitUntaintedDesc, false);
    }
}
