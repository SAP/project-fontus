package de.tubs.cs.ias.asm_test.methodinstrumentation;

import de.tubs.cs.ias.asm_test.Descriptor;

public interface MethodInstrumentationStrategy {
    //void instrumentReturnType(Descriptor desc);

    Descriptor rewriteDescriptor(Descriptor desc);
    boolean instrumentFieldIns(final int opcode, final String owner, final String name, final String descriptor);

    void insertJdkMethodParameterConversion(String parameter);

    boolean rewriteOwnerMethod(final int opcode,
                               final String owner,
                               final String name,
                               final String descriptor,
                               final boolean isInterface);
}
