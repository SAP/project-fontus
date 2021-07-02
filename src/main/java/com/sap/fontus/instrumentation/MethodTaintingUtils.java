package com.sap.fontus.instrumentation;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.instrumentation.strategies.InstrumentationHelper;
import com.sap.fontus.instrumentation.strategies.method.MethodInstrumentationStrategy;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodTaintingUtils {
    private static final CombinedExcludedLookup lookup = new CombinedExcludedLookup(null);
    /**
     * Functional interfaces or packages with func interfaces which are JDK or excluded but should still not be uninstrumented as lmabda
     */
    private static final String[] lambdaIncluded = new String[]{"java/util/function/", "java/lang/"};

    /**
     * If a taint-aware string is on the top of the stack, we can call this function to add a check to handle tainted strings.
     */
    public static void callCheckTaintGeneric(MethodVisitor mv, String typeDescriptor, String sink) {
    }

    /**
     * Pushes an integer onto the stack.
     * Optimizes small integers towards their dedicated ICONST_n instructions to save space.
     */
    public static void pushNumberOnTheStack(MethodVisitor mv, int num) {
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

    public static String generateProxyLambdaMethod(String methodName) {
        return "$fontus$" + methodName;
    }

    public static boolean isMethodReferenceJdkOrExcluded(Handle realFunction) {
        return lookup.isPackageExcludedOrJdk(realFunction.getOwner());
    }

    public static boolean isLambdaCallJdkOrExcluded(String descriptor) {
        Descriptor desc = Descriptor.parseDescriptor(descriptor);
        Type instance = Type.getType(desc.getReturnType());

        boolean excluded = lookup.isPackageExcludedOrJdk(instance.getInternalName());
        for (String clsOrPackage : lambdaIncluded) {
            if (instance.getInternalName().startsWith(clsOrPackage)) {
                excluded = false;
                break;
            }
        }

        return excluded;
    }

    /**
     * Translates the call to a lambda function
     */
    static void invokeVisitLambdaCall(final TaintStringConfig configuration,
                                      MethodVisitor mv,
                                      List<MethodInstrumentationStrategy> strategies,
                                      Descriptor instrumentedProxyDescriptor,
                                      final String name,
                                      final String descriptor,
                                      final Handle bootstrapMethodHandle,
                                      final Object... bootstrapMethodArguments) {
        Descriptor desc = Descriptor.parseDescriptor(descriptor);
        Handle realFunction = (Handle) bootstrapMethodArguments[1];

        boolean isExcludedOrJdk = isLambdaCallJdkOrExcluded(descriptor) && !isMethodReferenceJdkOrExcluded(realFunction);

        Object[] bsArgs;
        if (!isExcludedOrJdk) {
            bsArgs = new Object[bootstrapMethodArguments.length];
            for (int i = 0; i < bootstrapMethodArguments.length; i++) {
                Object arg = bootstrapMethodArguments[i];
                if (arg instanceof Handle) {
                    Handle a = (Handle) arg;
                    bsArgs[i] = Utils.instrumentHandle(a, configuration, strategies);
                } else if (arg instanceof Type) {
                    Type a = (Type) arg;
                    if (a.getSort() == Type.OBJECT) {
                        bsArgs[i] = Type.getObjectType(InstrumentationHelper.getInstance(configuration).instrumentQN(a.getInternalName()));
                    } else {
                        bsArgs[i] = Utils.instrumentType(a, configuration);
                    }
                } else {
                    bsArgs[i] = arg;
                }
            }
        } else {
            bsArgs = bootstrapMethodArguments.clone();
            bsArgs[1] = new Handle(((Handle) bsArgs[1]).getTag(), ((Handle) bsArgs[1]).getOwner(), MethodTaintingUtils.generateProxyLambdaMethod(((Handle) bsArgs[1]).getName()), instrumentedProxyDescriptor.toDescriptor(), ((Handle) bsArgs[1]).isInterface());
        }
        String descr = InstrumentationHelper.getInstance(configuration).instrumentForNormalCall(desc).toDescriptor();


//        Handle instrumentedBootstrapHandle = new Handle(bootstrapMethodHandle.getTag(), Type.getInternalName(LambdaMetafactory.class), bootstrapMethodHandle.getName(), bootstrapMethodHandle.getDesc(), bootstrapMethodHandle.isInterface());
        mv.visitInvokeDynamicInsn(name, descr, bootstrapMethodHandle, bsArgs);
    }
}
