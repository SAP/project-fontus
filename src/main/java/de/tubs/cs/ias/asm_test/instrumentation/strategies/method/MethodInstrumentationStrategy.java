package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;
import org.objectweb.asm.Type;

/**
 * Interface containing the various tasks a MethodVisitor might want to execute to instrument the code inside a method.
 */
public interface MethodInstrumentationStrategy {

    /**
     * Called for @link{ {@link org.objectweb.asm.MethodVisitor#visitFieldInsn(int, String, String, String)}} calls.
     * <p>
     * Replaces the Field with an instrumented equivalent.
     *
     * @return Has any special handling taken place?
     */
    boolean instrumentFieldIns(final int opcode, final String owner, final String name, final String descriptor);

    /**
     * Inserts conversion instructions if the {@param parameter} corresponds wo the actual strategy.
     */
    boolean insertJdkMethodParameterConversion(String parameter);

    /**
     * Rewrites methods belonging to the class the actual strategy corresponds to.
     * <p>
     * For an explanation of the parameters see {@link org.objectweb.asm.MethodVisitor#visitMethodInsn(int, String, String, String, boolean)}.
     *
     * @return An instrumented FunctionCall the owner matches the strategy. Otherwise null.
     */
    FunctionCall rewriteOwnerMethod(final FunctionCall functionCall);

    void instrumentReturnType(String owner, String name, Descriptor desc);

    boolean handleLdc(final Object value);

    boolean handleLdcType(Type type);

    boolean handleLdcArray(Type type);

    String rewriteTypeIns(String type);
}
