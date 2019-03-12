import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;


import static org.objectweb.asm.Opcodes.*;

public class StringMethodRewriter extends ClassVisitor {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Collection<BlackListEntry> blacklist = new ArrayList<>();

    StringMethodRewriter(ClassVisitor cv) {
        super(ASM7, cv);

        this.blacklist.add(new BlackListEntry("main", "([Ljava/lang/String;)V", ACC_PUBLIC + ACC_STATIC));
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor,
                                   String signature, Object value) {

        Matcher descMatcher = Constants.strPattern.matcher(descriptor);
        if(descMatcher.find()) {
            logger.info("Replacing field {}:{} ({})", access, name, descriptor);
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            return super.visitField(access, name, newDescriptor, signature, value);
        } else {
            return super.visitField(access, name, descriptor, signature, value);
        }
    }

    // Doesn't quite work yet
    // TODO: need to get name of owner class, work out how..!
    private void createMainWrapperMethod(MethodVisitor mv) {
        mv.visitCode();
        Label label0 = new Label();
        mv.visitLabel(label0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitTypeInsn(ANEWARRAY, "IASString");
        mv.visitVarInsn(ASTORE, 1);
        Label label1 = new Label();
        mv.visitLabel(label1);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 2);
        Label label2 = new Label();
        mv.visitLabel(label2);
        mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"[LIASString;", Opcodes.INTEGER}, 0, null);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARRAYLENGTH);
        Label label3 = new Label();
        mv.visitJumpInsn(IF_ICMPGE, label3);
        Label label4 = new Label();
        mv.visitLabel(label4);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitTypeInsn(NEW, "IASString");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitInsn(AALOAD);
        mv.visitMethodInsn(INVOKESPECIAL, "IASString", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(AASTORE);
Label label5 = new Label();
        mv.visitLabel(label5);
        mv.visitIincInsn(2, 1);
        mv.visitJumpInsn(GOTO, label2);
        mv.visitLabel(label3);
        mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "TestString", "nmain", "([LIASString;)V", false);
Label label6 = new Label();
        mv.visitLabel(label6);
        mv.visitInsn(RETURN);
        mv.visitMaxs(6, 3);
        mv.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        Matcher descMatcher = Constants.strPattern.matcher(descriptor);
        MethodVisitor mv;
        /*if(access == 0x0009 && name.equals("main") && descriptor.equals("([Ljava/lang/String;)V")) {
            MethodVisitor v = super.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            createMainWrapperMethod(v);
            mv = super.visitMethod(access, "nmain", "([LIASString;)V", signature, exceptions);
        } else*/ if (!this.blacklist.contains(new BlackListEntry(name, descriptor, access)) && descMatcher.find()) {
            logger.info("Rewriting method signature {} ({})", name, descriptor);
            String newDescriptor = descMatcher.replaceAll(Constants.TStringDesc);
            mv = super.visitMethod(access, name, newDescriptor, signature, exceptions);
        } else {
            mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        return new StringRewriterVisitor(mv);
    }
}
