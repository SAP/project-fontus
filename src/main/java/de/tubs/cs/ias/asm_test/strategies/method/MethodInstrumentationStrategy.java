package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Descriptor;
import org.objectweb.asm.Type;

/**
 * Interface containing the various tasks a MethodVisitor might want to execute to instrument the code inside a method.
 */
public interface MethodInstrumentationStrategy {

    /**
     * Rewrites a {@link Descriptor}, replacing all occurrences of a certain type with its taint-aware counterpart.
     */
    Descriptor instrument(Descriptor desc);

    /**
     * Called for @link{ {@link org.objectweb.asm.MethodVisitor#visitFieldInsn(int, String, String, String)}} calls.
     *
     * Replaces the Field with an instrumented equivalent.
     *
     * @return Has any special handling taken place?
     */
    boolean instrumentFieldIns(final int opcode, final String owner, final String name, final String descriptor);

    /**
     * Inserts conversion instructions if the {@param parameter} corresponds wo the actual strategy.
     */
    void insertJdkMethodParameterConversion(String parameter);

    /**
     * Rewrites methods belonging to the class the actual strategy corresponds to.
     *
     * For an explanation of the parameters see {@link org.objectweb.asm.MethodVisitor#visitMethodInsn(int, String, String, String, boolean)}.
     *
     * @return Whether the owner matches the strategy.
     */
    boolean rewriteOwnerMethod(final int opcode,
                               final String owner,
                               final String name,
                               final String descriptor,
                               final boolean isInterface);

    void instrumentReturnType(String owner, String name, Descriptor desc);

    boolean handleLdc(final Object value);
    boolean handleLdcType(Type type);
    String rewriteTypeIns(String type);
}
