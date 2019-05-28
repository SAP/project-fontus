package de.tubs.cs.ias.asm_test;

@FunctionalInterface
interface MethodInvocation {
    void invoke(final int opcode,
                final String owner,
                final String name,
                final String descriptor,
                final boolean isInterface
    );
}
