import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class StringRewriterVisitor extends MethodVisitor {
    private static final Logger LOGGER = Logger.getLogger(StringRewriterVisitor.class.getName());

    private final HashMap<ProxiedFunctionEntry, Runnable> methodProxies;
    private final HashMap<ProxiedDynamicFunctionEntry, Runnable> dynProxies;

    StringRewriterVisitor(MethodVisitor methodVisitor) {
        super(ASM7, methodVisitor);
        this.methodProxies = new HashMap<>();
        this.dynProxies = new HashMap<>();

        this.fillProxies();
    }

    private void fillProxies() {
        this.dynProxies.put(new ProxiedDynamicFunctionEntry("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, Constants.TString, "concat", "(LIASString;LIASString;)LIASString;", false));
        this.methodProxies.put(
                new ProxiedFunctionEntry("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                () -> super.visitMethodInsn(Opcodes.INVOKESTATIC, "PrintStreamProxies", "println", "(Ljava/io/PrintStream;LIASString;)V", false));
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
            LOGGER.info(String.format("Proxying dynamic call to %s (%s)", name, descriptor));
            Runnable pf = this.dynProxies.get(pdfe);
            pf.run();
            return true;
        }
        return false;
    }

        private boolean tryToApplyProxyCall(String owner, String name, String descriptor) {
        ProxiedFunctionEntry pfe = new ProxiedFunctionEntry(owner, name, descriptor);
        if(this.methodProxies.containsKey(pfe)) {
            LOGGER.info(String.format("Proxying call to %s:%s (%s)", owner, name, descriptor));
            Runnable pf = this.methodProxies.get(pfe);
            pf.run();
            return true;
        }
        return false;
    }

    private static String opcodeToString(int opcode) {
        switch(opcode) {
            case INVOKEVIRTUAL: return "v";
            case INVOKEDYNAMIC: return "d";
            case INVOKESTATIC: return "s";
            case INVOKEINTERFACE: return "i";
            case INVOKESPECIAL: return "sp";
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


        // Don't rewrite Java standard library functions or IASString functions
        boolean skipInvoke = owner.contains("java") || owner.contains(Constants.TString);

        Matcher descMatcher = Constants.strPattern.matcher(descriptor);
        if(descMatcher.find() && !skipInvoke) {
            LOGGER.info(String.format("Rewriting invoke [%s] %s:%s (%s)", opcodeToString(opcode), owner, name, descriptor));
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else {
            LOGGER.info(String.format("Skipping invoke [%s] %s:%s (%s)", opcodeToString(opcode), owner, name, descriptor));
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

        LOGGER.info(String.format("invokeDynamic %s (%s)", name, descriptor));
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);

    }

}
