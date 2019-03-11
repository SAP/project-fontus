import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;

public class MethodReplacer extends ClassVisitor {

    private String mName;
    private String mDesc;
    private String cName;

    MethodReplacer(ClassVisitor cv, String mName, String mDesc) {
        super(ASM7, cv);
        this.mName = mName;
        this.mDesc = mDesc;
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {
        this.cName = name;
        super.visit(version, access, name, signature, superName, interfaces);

    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        if (name.equals(mName) && descriptor.equals(mDesc)) {
            String newName = "orig$" + name;
            generateNewBody(access, name, descriptor, signature, exceptions, newName);
            return super.visitMethod(access, newName, descriptor, signature, exceptions);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    private void generateNewBody(final int access,
                                 final String name,
                                 final String descriptor,
                                 final String signature,
                                 final String[] exceptions, String newName) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Calling wrapper method for getValue()");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, cName, newName, descriptor, false);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1,1);
        mv.visitEnd();
    }

}
