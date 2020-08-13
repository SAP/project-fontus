package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.asm.FieldData;
import de.tubs.cs.ias.asm_test.asm.FunctionCall;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.method.MethodInstrumentationStrategy;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

public final class Utils {

    private Utils() {
    }

    public static String opcodeToString(int opcode) {
        switch (opcode) {
            case Opcodes.INVOKEVIRTUAL:
                return "v";
            case Opcodes.INVOKEDYNAMIC:
                return "d";
            case Opcodes.INVOKESTATIC:
                return "s";
            case Opcodes.INVOKEINTERFACE:
                return "i";
            case Opcodes.INVOKESPECIAL:
                return "sp";
            default:
                return "unknown";
        }
    }

    /**
     * How many local vars does the store operation described by opcode take up.
     *
     * @param opcode An opcode describing a store operation, e.g., ASTORE
     * @return 1 for type 1 values, 2 for type 2 values.
     */
    public static int storeOpcodeSize(int opcode) {
        return (opcode == Opcodes.LSTORE || opcode == Opcodes.DSTORE ? 2 : 1);
    }

    public static Type instrumentType(Type t, TaintStringConfig config) {
        Descriptor desc = Descriptor.parseDescriptor(t.getDescriptor());
        desc = InstrumentationHelper.getInstance(config).instrument(desc);
        return Type.getType(desc.toDescriptor());
    }

    public static Handle instrumentHandle(Handle h, TaintStringConfig config, List<MethodInstrumentationStrategy> strategies) {
        if (JdkClassesLookupTable.getInstance().isJdkClass(h.getOwner()) && !InstrumentationHelper.getInstance(config).canHandleType(Type.getObjectType(h.getOwner()).getDescriptor())) {
            return h;
        }

        // If the Class is a taintaware one it should be handled by rewriteOwnerMethod, e.g. for toString => toIASString
        for (MethodInstrumentationStrategy s : strategies) {
            FunctionCall instrumented = s.rewriteOwnerMethod(new FunctionCall(h.getTag(), h.getOwner(), h.getName(), h.getDesc(), h.isInterface()));
            if (instrumented != null) {
                return new Handle(instrumented.getOpcode(), instrumented.getOwner(), instrumented.getName(), instrumented.getDescriptor(), instrumented.isInterface());
            }
        }

        Descriptor desc = Descriptor.parseDescriptor(h.getDesc());
        desc = InstrumentationHelper.getInstance(config).instrument(desc);
        String owner = InstrumentationHelper.getInstance(config).instrumentQN(h.getOwner());
        return new Handle(h.getTag(), owner, h.getName(), desc.toDescriptor(), h.isInterface());
    }

    public static String fixup(String s) {
        return s.replace('/', '.');
    }

    public static String fixupReverse(String s) {
        return s.replace('.', '/');
    }

    public static void writeToStaticInitializer(MethodVisitor mv, String owner, Iterable<FieldData> staticFields) {
        for (FieldData field : staticFields) {
            Object value = field.getValue();
            mv.visitLdcInsn(value);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, field.getName(), field.getDescriptor());
        }
    }

    public static boolean contains(String[] array, String value) {
        for (String member : array) {
            if (member.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
