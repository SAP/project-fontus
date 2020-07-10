package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.utils.Utils;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class MethodTaintingUtils {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * If a taint-aware string is on the top of the stack, we can call this function to add a check to handle tainted strings.
     */
    public static void callCheckTaint(MethodVisitor mv, TaintStringConfig configuration) {
        Label after = new Label();
        // Call dup here to put the TString reference twice on the stack so the call can pop one without affecting further processing
        mv.visitInsn(Opcodes.DUP);
        mv.visitJumpInsn(Opcodes.IFNULL, after);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, configuration.getTStringQN(), Constants.ABORT_IF_TAINTED, "()V", false);
        mv.visitLabel(after);
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
        if (!types.containsKey(type)) return;

        String full = types.get(type);
        String owner = String.format("java/lang/%s", full);
        String desc = String.format("(%s)L%s;", type, owner);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, Constants.VALUE_OF, desc, false);
    }

    /**
     * Translates the call to a lambda function
     */
    static void invokeVisitLambdaCall(final TaintStringConfig configuration,
                                      MethodVisitor mv,
                                      final String name,
                                      final String descriptor,
                                      final Handle bootstrapMethodHandle,
                                      final Object... bootstrapMethodArguments) {
        Object[] bsArgs = new Object[bootstrapMethodArguments.length];
        for (int i = 0; i < bootstrapMethodArguments.length; i++) {
            Object arg = bootstrapMethodArguments[i];
            if (arg instanceof Handle) {
                Handle a = (Handle) arg;
                bsArgs[i] = Utils.instrumentHandle(a, configuration);
            } else if (arg instanceof Type) {
                Type a = (Type) arg;
                bsArgs[i] = Utils.instrumentType(a, configuration);
            } else {
                bsArgs[i] = arg;
            }
        }
        Descriptor desc = Descriptor.parseDescriptor(descriptor);
        String descr = InstrumentationHelper.getInstance(configuration).instrument(desc).toDescriptor();
        mv.visitInvokeDynamicInsn(name, descr, bootstrapMethodHandle, bsArgs);
    }

}
