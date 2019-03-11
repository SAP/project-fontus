import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class StringRewriterVisitor extends MethodVisitor {
    private final Pattern strPattern = Pattern.compile("Ljava/lang/String\\b");
    private final static Logger LOGGER = Logger.getLogger(StringRewriterVisitor.class.getName());

    StringRewriterVisitor(MethodVisitor methodVisitor) {
        super(ASM7, methodVisitor);
    }

    @Override
    public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor) {
        Matcher descMatcher = this.strPattern.matcher(descriptor);

        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll("LIASString");
            super.visitFieldInsn(opcode, owner, name, newDescriptor);
        } else {
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }

    @Override
    public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
        LOGGER.info(String.format("invoke %d %s:%s (%s)", opcode, owner, name, descriptor));

        Matcher descMatcher = this.strPattern.matcher(descriptor);
        if(owner.contains("java") || owner.contains("IASString")) {
            // handle JDK methods by putting IASStrings on teh stack
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        }
        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll("LIASString");
            super.visitMethodInsn(opcode, owner, name, newDescriptor, isInterface);
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }



    @Override
    public void visitLdcInsn(final Object value) {
        if(value instanceof String) {
            Label label1 = new Label();
            this.visitLabel(label1);
            this.visitLineNumber(18, label1);
            this.visitTypeInsn(Opcodes.NEW, "IASString");
            this.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(value);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "IASString", "<init>", "(Ljava/lang/String;)V", false);
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
            super.visitMethodInsn(INVOKESTATIC, "IASString", "concat", "(LIASString;LIASString;)LIASString;", false);
        } else {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        }
    }

}
