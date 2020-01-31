package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Descriptor;
import de.tubs.cs.ias.asm_test.strategies.StringInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class FormatterMethodInstrumentationStrategy extends StringInstrumentation implements MethodInstrumentationStrategy {
    private final MethodVisitor mv;

    public FormatterMethodInstrumentationStrategy(MethodVisitor parentVisitor) {
        this.mv = parentVisitor;
    }

    @Override
    public boolean instrumentFieldIns(int opcode, String owner, String name, String descriptor) {
        return false;
    }

    @Override
    public void insertJdkMethodParameterConversion(String parameter) {

    }

    @Override
    public boolean rewriteOwnerMethod(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        return false;
    }

    @Override
    public void instrumentReturnType(String owner, String name, Descriptor desc) {

    }

    @Override
    public boolean handleLdc(Object value) {
        return false;
    }

    @Override
    public boolean handleLdcType(Type type) {
        return false;
    }

    @Override
    public boolean handleLdcArray(Type type) {
        return false;
    }

    @Override
    public String rewriteTypeIns(String type) {
        return null;
    }
}
