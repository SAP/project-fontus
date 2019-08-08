package de.tubs.cs.ias.asm_test.methodinstrumentation;

import de.tubs.cs.ias.asm_test.Constants;
import org.objectweb.asm.MethodVisitor;

import java.util.regex.Matcher;

public class StringBufferMethodInstrumentationStrategy implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;

    public StringBufferMethodInstrumentationStrategy(MethodVisitor mv) {
        this.mv = mv;
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        Matcher matcher = Constants.strBufferPattern.matcher(descriptor);
        if (matcher.find()) {
            String newDescriptor = matcher.replaceAll(Constants.TStringBufferDesc);
            this.mv.visitFieldInsn(opcode, owner, name, newDescriptor);
            return true;
        }
        return false;
    }
}
