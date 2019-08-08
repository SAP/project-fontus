package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern STRING_QN_MATCHER = Pattern.compile(Constants.StringQN, Pattern.LITERAL);
    private static final Pattern STRING_BUILDER_QN_MATCHER = Pattern.compile(Constants.StringBuilderQN, Pattern.LITERAL);

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
     * Returns the Opcode to store an instance of a value described by type.
     *
     * @param type One of Z, B, C, I, J, D, F or a class type.
     * @return The store Opcode, e.g., ASTORE
     */
    static int getStoreOpcode(String type) {
        switch (type) {
            case "Z":
            case "B":
            case "C":
            case "I":
                return Opcodes.ISTORE;
            case "J":
                return Opcodes.LSTORE;
            case "D":
                return Opcodes.DSTORE;
            case "F":
                return Opcodes.FSTORE;
            default:
                return Opcodes.ASTORE;
        }
    }

    /**
     * How many local vars does the store operation described by opcode take up.
     *
     * @param opcode An opcode describing a store operation, e.g., ASTORE
     * @return 1 for type 1 values, 2 for type 2 values.
     */
    static int storeOpcodeSize(int opcode) {
        return (opcode == Opcodes.LSTORE || opcode == Opcodes.DSTORE ? 2 : 1);
    }

    /**
     * Returns the Opcode to load an instance of a value described by type.
     *
     * @param type One of Z, B, C, I, J, D, F or a class type.
     * @return The store Opcode, e.g., ALOAD
     */
    static int getLoadOpcode(String type) {
        switch (type) {
            case "Z":
            case "B":
            case "C":
            case "I":
                return Opcodes.ILOAD;
            case "J":
                return Opcodes.LLOAD;
            case "D":
                return Opcodes.DLOAD;
            case "F":
                return Opcodes.FLOAD;
            default:
                return Opcodes.ALOAD;
        }
    }

    static Type instrumentType(Type t) {
        String desc = t.getDescriptor();
        Descriptor d = Descriptor.parseDescriptor(desc);
        d = d.replaceType(Constants.StringDesc, Constants.TStringDesc);
        d = d.replaceType(Constants.StringBuilderDesc, Constants.TStringBuilderDesc);
        return Type.getType(d.toDescriptor());

    }


    static Handle instrumentHandle(Handle h) {
        String desc = h.getDesc();
        desc = Constants.strPattern.matcher(desc).replaceAll(Constants.TStringDesc);
        desc = Constants.strBuilderPattern.matcher(desc).replaceAll(Constants.TStringBuilderDesc);
        String owner = h.getOwner();
        owner = STRING_QN_MATCHER.matcher(owner).replaceAll(Matcher.quoteReplacement(Constants.TStringQN));
        owner = STRING_BUILDER_QN_MATCHER.matcher(owner).replaceAll(Matcher.quoteReplacement(Constants.TStringBuilderQN));
        return new Handle(h.getTag(), owner, h.getName(), desc, h.isInterface());
    }

    static String translateClassName(String className) {
        if (className.equals(fixup(Constants.StringQN))) {
            return fixup(Constants.TStringQN);
        } else if (className.equals(fixup(Constants.StringBuilderQN))) {
            return fixup(Constants.TStringBuilderQN);
        } else {
            return className;
        }
    }

    // Duplication with IASReflectionProxies, but we don't want to add all that many class files to the utils jar..
    private static String fixup(String s) {
        return s.replace('/', '.');
    }

    static void writeToStaticInitializer(MethodVisitor mv, String owner, Iterable<Tuple<Tuple<String, String>, Object>> staticFields) {
        for (Tuple<Tuple<String, String>, Object> e : staticFields) {
            Object value = e.y;
            Tuple<String, String> field = e.x;
            mv.visitLdcInsn(value);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, field.x, field.y);
        }
    }

    static String rewriteDescriptor(String desc) {
        String newDescriptor = desc.replaceAll(Constants.StringDesc, Constants.TStringDesc);
        newDescriptor = newDescriptor.replaceAll(Constants.StringBufferDesc, Constants.TStringBufferDesc);
        newDescriptor = newDescriptor.replaceAll(Constants.StringBuilderDesc, Constants.TStringBuilderDesc);
        return newDescriptor;
    }
}
