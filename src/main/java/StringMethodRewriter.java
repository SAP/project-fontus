import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ASM7;

public class StringMethodRewriter extends ClassVisitor {
    private final Pattern strPattern = Pattern.compile("Ljava/lang/String\\b");
    private final Collection<BlackListEntry> blacklist = new ArrayList<>();

    public StringMethodRewriter(ClassVisitor cv) {
        super(ASM7, cv);
        this.blacklist.add(new BlackListEntry("main", "([Ljava/lang/String;)V", 0x0009));

    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor,
                                   String signature, Object value) {
        Matcher descMatcher = this.strPattern.matcher(descriptor);
        if(descMatcher.find()) {
            String newDescriptor = descMatcher.replaceAll("LIASString");

            return super.visitField(access, name, newDescriptor, signature, value);
        } else {
            return super.visitField(access, name, descriptor, signature, value);
        }
    }

        @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {
        Matcher descMatcher = this.strPattern.matcher(descriptor);
        if (!this.blacklist.contains(new BlackListEntry(name, descriptor, access)) && descMatcher.find()) {
            System.out.println("Replacing " + name + "(" + descriptor + ")");
            String newDescriptor = descMatcher.replaceAll("LIASString");
            return new StringRewriterVisitor(super.visitMethod(access, name, newDescriptor, signature, exceptions));
        }

        return new StringRewriterVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
    }

}
