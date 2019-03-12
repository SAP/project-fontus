import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class StringRewriterVisitor extends MethodVisitor {
    private static final Logger LOGGER = Logger.getLogger(StringRewriterVisitor.class.getName());

    private final HashMap<ProxiedFunctionEntry, ProxyFunction> proxies;

    StringRewriterVisitor(MethodVisitor methodVisitor) {
        super(ASM7, methodVisitor);
        this.proxies = new HashMap<>();
        this.fillProxies();
    }

    private void fillProxies() {
        this.proxies.put(
                new ProxiedFunctionEntry("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                () -> super.visitMethodInsn(INVOKESTATIC, "PrintStreamProxies", "println", "(Ljava/io/PrintStream;LIASString;)V", false));
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

    private boolean needsToApplyProxyCall(String owner, String name, String descriptor) {
        ProxiedFunctionEntry pfe = new ProxiedFunctionEntry(owner, name, descriptor);
        if(this.proxies.containsKey(pfe)) {
            LOGGER.info(String.format("Proxying call to %s:%s (%s)", owner, name, descriptor));
            ProxyFunction pf = this.proxies.get(pfe);
            pf.apply();
            return true;
        }
        return false;
    }

    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
        LOGGER.info(String.format("invoke %d %s:%s (%s)", opcode, owner, name, descriptor));

        Matcher descMatcher = Constants.strPattern.matcher(descriptor);
        if(this.needsToApplyProxyCall(owner, name, descriptor)) { return; }

        if(owner.contains("java") || owner.contains(Constants.TString)) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        }
        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }



    @Override
    public void visitLdcInsn(final Object value) {
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
        LOGGER.info(String.format("invokeDynamic %s (%s)", name, descriptor));
        if("makeConcatWithConstants".equals(name) && "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;".equals(descriptor)) {
            super.visitMethodInsn(INVOKESTATIC, Constants.TString, "concat", "(LIASString;LIASString;)LIASString;", false);
        } else {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }
    }

}
