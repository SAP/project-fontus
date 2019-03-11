import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;

public class NumberReplacer extends ClassVisitor {

    public NumberReplacer(ClassVisitor classVisitor) {
        super(ASM7, classVisitor);
    }


    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        if("LNumber;".equals(desc)) {
            return super.visitField(access, name, "LExtNumber;", signature, value);
        } else {
            return super.visitField(access, name, desc, signature, value);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    class MethodNumberReplacer extends MethodVisitor {


        public MethodNumberReplacer() {
            super(ASM7);
        }

    }
}
