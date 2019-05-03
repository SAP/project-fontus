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

    MethodTaintingVisitor(MethodVisitor methodVisitor) {
        super(Opcodes.ASM7, methodVisitor);
        this.methodProxies = new HashMap<>();
        this.dynProxies = new HashMap<>();

        this.fillProxies();
    }

    private void fillProxies() {
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
                new ProxiedFunctionEntry("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                () -> {
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Constants.TString, "getString", "()Ljava/lang/String;", false);
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                });
    }

    @Override
    public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor) {
        Matcher descMatcher = Constants.strPattern.matcher(descriptor);

        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            super.visitFieldInsn(opcode, owner, name, newDescriptor);
        } else {
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }

    private boolean tryToApplyDynProxyCall(String name, String descriptor) {
        ProxiedDynamicFunctionEntry pdfe = new ProxiedDynamicFunctionEntry(name, descriptor);
        if(this.dynProxies.containsKey(pdfe)) {
            logger.info("Proxying dynamic call to {} ({})", name, descriptor);
            Runnable pf = this.dynProxies.get(pdfe);
            pf.run();
            return true;
        }
        return false;
    }

        private boolean tryToApplyProxyCall(String owner, String name, String descriptor) {
        ProxiedFunctionEntry pfe = new ProxiedFunctionEntry(owner, name, descriptor);
        if(this.methodProxies.containsKey(pfe)) {
            logger.info("Proxying call to {}:{} ({})", owner, name, descriptor);
            Runnable pf = this.methodProxies.get(pfe);
            pf.run();
            return true;
        }
        return false;
    }

    private static String opcodeToString(int opcode) {
        switch(opcode) {
            case Opcodes.INVOKEVIRTUAL: return "v";
            case Opcodes.INVOKEDYNAMIC: return "d";
            case Opcodes.INVOKESTATIC: return "s";
            case Opcodes.INVOKEINTERFACE: return "i";
            case Opcodes.INVOKESPECIAL: return "sp";
            default: return "unknown";
        }
    }
    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
        // If a method has a defined proxy, apply it right away
        if(this.tryToApplyProxyCall(owner, name, descriptor)) { return; }

        Matcher descMatcher = Constants.strPattern.matcher(descriptor);

        if("java/lang/String".equals(owner)) {
            String newOwner = Constants.TString;
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            logger.info("Rewriting String invoke [{}] {}:{} ({})", opcodeToString(opcode), owner, name, newDescriptor);
            super.visitMethodInsn(opcode, newOwner, name, newDescriptor, isInterface);
            return;
        }

        // Don't rewrite Java standard library functions or IASString functions
        boolean skipInvoke = owner.contains("java") || owner.contains(Constants.TString);
        if(descMatcher.find() && !skipInvoke) {
            logger.info("Rewriting invoke [{}] {}:{} ({})", opcodeToString(opcode), owner, name, descriptor);
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else {
            logger.info("Skipping invoke [{}] {}:{} ({})", opcodeToString(opcode), owner, name, descriptor);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    @Override
    public void visitLdcInsn(final Object value) {
        // When loading a constant, make a taintable string out of a string constant.
        if(value instanceof String) {
            this.visitTypeInsn(Opcodes.NEW, Constants.TString);
            this.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(value);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, Constants.TString, "<init>", "(Ljava/lang/String;)V", false);
        }
        else {
            super.visitLdcInsn(value);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {

        if(this.tryToApplyDynProxyCall(name, descriptor)) { return; }

        logger.info("invokeDynamic {} ({})", name, descriptor);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);

    }

}
