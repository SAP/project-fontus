package de.tubs.cs.ias.asm_test.methodinstrumentation;

import de.tubs.cs.ias.asm_test.Descriptor;
import org.objectweb.asm.Type;

public interface MethodInstrumentationStrategy {

    Descriptor rewriteDescriptor(Descriptor desc);
    boolean instrumentFieldIns(final int opcode, final String owner, final String name, final String descriptor);

    void insertJdkMethodParameterConversion(String parameter);

    boolean rewriteOwnerMethod(final int opcode,
                               final String owner,
                               final String name,
                               final String descriptor,
                               final boolean isInterface);

    void instrumentReturnType(String owner, String name, Descriptor desc);

    boolean handleLdc(final Object value);
    boolean handleLdcType(Type type);
}
