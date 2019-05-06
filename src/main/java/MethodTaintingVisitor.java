import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MethodTaintingVisitor extends MethodVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HashMap<ProxiedFunctionEntry, Runnable> methodProxies;
    private final HashMap<ProxiedDynamicFunctionEntry, Runnable> dynProxies;
    private final HashMap<String, String> stringBuilderMethodsToRename;

    MethodTaintingVisitor(MethodVisitor methodVisitor) {
        super(Opcodes.ASM7, methodVisitor);
        this.methodProxies = new HashMap<>();
        this.dynProxies = new HashMap<>();
        this.stringBuilderMethodsToRename = new HashMap<>();
        this.fillProxies();
        this.fillMethodsToRename();
    }



    /**
     *  Initializes the methods that shall be renamed map.
     */
    private void fillMethodsToRename() {
        // TODO: make dynamic!
        this.stringBuilderMethodsToRename.put("toString", "toIASString");
    }

    /**
     *  Initializes the method proxy maps.
     */
    private void fillProxies() {
        // TODO: make dynamic!
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;LIASString;)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;I)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;I)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;J)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;J)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;D)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;D)LIASString;", false));
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;F)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;F)LIASString;", false));

        this.methodProxies.put(
                new ProxiedFunctionEntry("java/lang/Integer", "parseInt", "(Ljava/lang/String;)I"),
                () -> {
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, "getString", "()Ljava/lang/String;", false);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
                }
        );
        this.methodProxies.put(
                new ProxiedFunctionEntry("java/lang/Integer", "toString", "()Ljava/lang/String;"),
                () -> {
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "toString", "()Ljava/lang/String;", false);
                    super.visitTypeInsn(Opcodes.NEW, Constants.TString);
                    super.visitInsn(Opcodes.DUP);
                    super.visitInsn(Opcodes.DUP2_X1);
                    super.visitInsn(Opcodes.POP2);
                    super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TString, Constants.Init, "(Ljava/lang/String;)V", false);
                }
        );
        this.methodProxies.put(
                new ProxiedFunctionEntry("java/lang/Integer", "toString", "(I)Ljava/lang/String;"),
                () -> {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false);
                    super.visitTypeInsn(Opcodes.NEW, Constants.TString);
                    super.visitInsn(Opcodes.DUP);
                    super.visitInsn(Opcodes.DUP2_X1);
                    super.visitInsn(Opcodes.POP2);
                    super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TString, Constants.Init, "(Ljava/lang/String;)V", false);
                }
        );
        this.methodProxies.put(
                new ProxiedFunctionEntry("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                () -> {
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, "getString", "()Ljava/lang/String;", false);
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                }
        );
    }

    private boolean tryToApplyDynProxyCall(String name, String descriptor) {
        ProxiedDynamicFunctionEntry pdfe = new ProxiedDynamicFunctionEntry(name, descriptor);
        if (this.dynProxies.containsKey(pdfe)) {
            logger.info("Proxying dynamic call to {} ({})", name, descriptor);
            Runnable pf = this.dynProxies.get(pdfe);
            pf.run();
            return true;
        }
        return false;
    }

    private boolean tryToApplyProxyCall(String owner, String name, String descriptor) {
        ProxiedFunctionEntry pfe = new ProxiedFunctionEntry(owner, name, descriptor);
        if (this.methodProxies.containsKey(pfe)) {
            logger.info("Proxying call to {}:{} ({})", owner, name, descriptor);
            Runnable pf = this.methodProxies.get(pfe);
            pf.run();
            return true;
        }
        return false;
    }

    /**
     * Visit a method belonging to java/lang/String
     */
    private void visitStringMethod(final int opcode,
                                   final String owner,
                                   final String name,
                                   final String descriptor,
                                   final boolean isInterface) {
        Matcher stringDescMatcher = Constants.strPattern.matcher(descriptor);
        String newOwner = Constants.TString;
        String newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
        logger.info("Rewriting String invoke [{}] {}:{}{} to {}:{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, name, newDescriptor);
        super.visitMethodInsn(opcode, newOwner, name, newDescriptor, isInterface);
    }

    /**
     * Visit a method belonging to java/lang/StringBuilder
     */
    private void visitStringBuilderMethod(final int opcode,
                                          final String owner,
                                          final String name,
                                          final String descriptor,
                                          final boolean isInterface) {
        Matcher sbDescMatcher = Constants.strBuilderPattern.matcher(descriptor);
        String newOwner = Constants.TStringBuilder;
        String newDescriptor = sbDescMatcher.replaceAll(Constants.TStringBuilderDesc);
        // Replace all instances of java/lang/String
        Matcher newDescriptorMatcher = Constants.strPattern.matcher(newDescriptor);
        String finalDescriptor = newDescriptorMatcher.replaceAll(Constants.TStringDesc);
        String newName = this.stringBuilderMethodsToRename.getOrDefault(name, name);

        logger.info("Rewriting StringBuilder invoke [{}] {}:{}{} to {}:{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, newOwner, newName, finalDescriptor);
        super.visitMethodInsn(opcode, newOwner, newName, finalDescriptor, isInterface);
    }

    @Override
    public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor) {
        Matcher descMatcher = Constants.strPattern.matcher(descriptor);
        if (descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            super.visitFieldInsn(opcode, owner, name, newDescriptor);
            return;
        }

        Matcher sbDescMatcher = Constants.strBuilderPattern.matcher(descriptor);
        if (sbDescMatcher.find()) {
            String newDescriptor = sbDescMatcher.replaceAll(Constants.TStringBuilderDesc);
            super.visitFieldInsn(opcode, owner, name, newDescriptor);
            return;
        }

        super.visitFieldInsn(opcode, owner, name, descriptor);
    }



    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
        // If a method has a defined proxy, apply it right away
        if (this.tryToApplyProxyCall(owner, name, descriptor)) {
            return;
        }

        // Method belongs to type java/lang/String -> rewrite
        if (Constants.String.equals(owner)) {
            this.visitStringMethod(opcode, owner, name, descriptor, isInterface);
            return;
        }

        // Method belongs to type java/lang/StringBuilder -> rewrite
        if (Constants.StringBuilder.equals(owner)) {
            this.visitStringBuilderMethod(opcode, owner, name, descriptor, isInterface);
            return;
        }

        // Don't rewrite Java standard library functions or IASString functions
        boolean skipInvoke = owner.contains("java") || owner.contains(Constants.TString) || owner.contains(Constants.TStringBuilder);

        Matcher stringBuilderdescMatcher = Constants.strBuilderPattern.matcher(descriptor);
        Matcher stringDescMatcher = Constants.strPattern.matcher(descriptor);
        // TODO: case when both find()s are true?
        if (stringDescMatcher.find() && !skipInvoke) {
            String newDescriptor = stringDescMatcher.replaceAll(Constants.TStringDesc);
            logger.info("Rewriting invoke containing String [{}] {}:{}{} to {}:{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, owner, name, newDescriptor);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else if (stringBuilderdescMatcher.find() && !skipInvoke) {
            String newDescriptor = stringBuilderdescMatcher.replaceAll(Constants.TStringBuilderDesc);
            logger.info("Rewriting invoke containing StringBuilder [{}] {}:{}{} to {}:{}{}", Utils.opcodeToString(opcode), owner, name, descriptor, owner, name, newDescriptor);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else {
            logger.info("Skipping invoke [{}] {}:{} ({})", Utils.opcodeToString(opcode), owner, name, descriptor);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    @Override
    public void visitLdcInsn(final Object value) {
        // When loading a constant, make a taint-aware string out of a string constant.
        if (value instanceof String) {
            logger.info("Rewriting String LDC to IASString LDC instruction");
            this.visitTypeInsn(Opcodes.NEW, Constants.TString);
            this.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(value);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TString, Constants.Init, "(Ljava/lang/String;)V", false);
        } else {
            super.visitLdcInsn(value);
        }
    }

    /**
     * We want to override some instantiations of classes with our own types
     */
    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        logger.info("Visiting type [{}] instruction: {}", type, opcode);
        switch (type) {
            case Constants.StringBuilder:
                super.visitTypeInsn(opcode, Constants.TStringBuilder);
                break;
            case Constants.String:
                super.visitTypeInsn(opcode, Constants.TString);
                break;
            default:
                super.visitTypeInsn(opcode, type);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {

        if (this.tryToApplyDynProxyCall(name, descriptor)) {
            return;
        }

        logger.info("invokeDynamic {} ({})", name, descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);

    }

}
