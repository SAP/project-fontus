import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class ClassPrinter extends ClassVisitor {
    ClassPrinter(ClassVisitor cv) {
        super(ASM7, cv);
    }

    private static String printVisibility(final int access) {
        if((access & ACC_PRIVATE) == ACC_PRIVATE) return "private";
        if((access & ACC_PUBLIC) == ACC_PUBLIC) return "public";
        if((access & ACC_PROTECTED) == ACC_PROTECTED) return "protected";
        return "internal";
    }

    private static boolean isStaticMethod(final int access) {
        return (access & ACC_STATIC) == ACC_STATIC;
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        String implement = "";
        if(interfaces.length > 0) {
            implement = " implements [" + String.join(", ", interfaces) + "] ";
        }
        System.out.printf("%s extends %s%s{%n", name, implement, superName);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc,
                                             boolean visible) {

        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
    }

    @Override
    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        System.out.printf("\tf[%s]: %s %s%n", printVisibility(access), desc, name);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        String prefix = isStaticMethod(access) ? "sm" : "m";
        System.out.printf("\t%s[%s]: %s%s %d,%s%n", prefix, printVisibility(access), name, desc, access, signature);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        System.out.println("}");
        super.visitEnd();
    }
}