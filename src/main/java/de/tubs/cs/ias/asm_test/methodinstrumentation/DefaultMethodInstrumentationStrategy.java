package de.tubs.cs.ias.asm_test.methodinstrumentation;

import de.tubs.cs.ias.asm_test.Descriptor;
import org.objectweb.asm.MethodVisitor;

public class DefaultMethodInstrumentationStrategy implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;
    public DefaultMethodInstrumentationStrategy(MethodVisitor mv) {
        this.mv = mv;
    }

    //@Override
    public void instrumentReturnType(Descriptor desc) {
    }

    @Override
    public Descriptor rewriteDescriptor(Descriptor desc) {
        return desc;
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        this.mv.visitFieldInsn(opcode, owner, name, descriptor);
        return true;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {

    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        return false;
    }
}
