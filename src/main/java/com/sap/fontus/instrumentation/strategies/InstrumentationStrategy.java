package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.TriConsumer;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.asm.FunctionCall;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Optional;

public interface InstrumentationStrategy {
    /**
     * Replaces all taintable type occurrences with the concrete type
     */
    Descriptor instrument(Descriptor desc);

    /**
     * Returns concrete type of the tainted type
     */
    String uninstrument(String typeDescriptor);

    /**
     * Returns concrete type of the tainted type
     */
    Descriptor uninstrumentForJdkCall(Descriptor descriptor);

    String instrumentQN(String qn);

    /**
     * Replaces taintable type occurences in the parameters with the interface and in the return with the concrete type
     * Used for instrumenting calls to taintable classes e.g. String/IASString.replaceAll
     */
//    String instrumentDescForIASCall(String desc);

    Optional<String> translateClassName(String className);

    boolean handlesType(String descriptor);

    boolean isInstrumented(String descriptor);

    Optional<FieldVisitor> instrumentFieldInstruction(ClassVisitor classVisitor, int access, String name, String descriptor,
                                                      String signature, Object value, TriConsumer tc);
    String instrumentSuperClass(String superClass);

    /**
     * Called for @link{ {@link org.objectweb.asm.MethodVisitor#visitFieldInsn(int, String, String, String)}} calls.
     * <p>
     * Replaces the Field with an instrumented equivalent.
     *
     * @return Has any special handling taken place?
     */
    boolean instrumentFieldIns(MethodVisitor mv, final int opcode, final String owner, final String name, final String descriptor);

    /**
     * Inserts conversion instructions if the {@param parameter} corresponds wo the actual strategy.
     */
    boolean insertJdkMethodParameterConversion(MethodVisitor mv, Type parameter);

    /**
     * Rewrites methods belonging to the class the actual strategy corresponds to.
     * <p>
     * For an explanation of the parameters see {@link org.objectweb.asm.MethodVisitor#visitMethodInsn(int, String, String, String, boolean)}.
     *
     * @return An instrumented FunctionCall the owner matches the strategy. Otherwise null.
     */
    FunctionCall rewriteOwnerMethod(final FunctionCall functionCall);

    void instrumentStackTop(MethodVisitor mv, Type origType);

    boolean handleLdc(MethodVisitor mv, final Object value);

    boolean handleLdcType(MethodVisitor mv, Type type);

    boolean handleLdcArray(MethodVisitor mv, Type type);

    String rewriteTypeIns(String type);
}
