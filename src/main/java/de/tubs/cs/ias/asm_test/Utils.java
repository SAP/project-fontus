package de.tubs.cs.ias.asm_test;

import org.objectweb.asm.Opcodes;

final class Utils {

    private Utils() {}

    static String opcodeToString(int opcode) {
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
     * @param type One of Z, B, C, I, J, D, F or a class type.
     * @return The store Opcode, e.g., ASTORE
     */
    static int getStoreOpcode(String type) {
        switch(type) {
            case "Z": case "B": case "C": case "I": return Opcodes.ISTORE;
            case "J": return Opcodes.LSTORE;
            case "D": return Opcodes.DSTORE;
            case "F": return Opcodes.FSTORE;
            default: return Opcodes.ASTORE;
        }
    }

    /**
     * How many local vars does the store operation described by opcode take up.
     * @param opcode An opcode describing a store operation, e.g., ASTORE
     * @return 1 for type 1 values, 2 for type 2 values.
     */
    static int storeOpcodeSize(int opcode) {
        return (opcode == Opcodes.LSTORE || opcode == Opcodes.DSTORE ? 2 : 1);
    }

    /**
     * Returns the Opcode to load an instance of a value described by type.
     * @param type One of Z, B, C, I, J, D, F or a class type.
     * @return The store Opcode, e.g., ALOAD
     */
    static int getLoadOpcode(String type) {
        switch(type) {
            case "Z": case "B": case "C": case "I": return Opcodes.ILOAD;
            case "J": return Opcodes.LLOAD;
            case "D": return Opcodes.DLOAD;
            case "F": return Opcodes.FLOAD;
            default: return Opcodes.ALOAD;
        }
    }

}
