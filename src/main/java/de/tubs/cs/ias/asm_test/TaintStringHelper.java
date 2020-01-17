package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

@SuppressWarnings({"ClassUnconnectedToPackage", "ClassOnlyUsedInOnePackage"})
public class TaintStringHelper {

    public static Boolean isTainted(String str) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static void setTaint(String str, Boolean b) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static Boolean isTainted(StringBuilder str) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static void setTaint(StringBuilder str, Boolean b) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static Boolean isTainted(StringBuffer str) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static void setTaint(StringBuffer str, Boolean b) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static String getString(String str) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static Boolean isTainted(IASTaintAware taintAware) {
        return taintAware.isTainted();
    }

    public static void setTaint(IASTaintAware taintAware, Boolean b) {
        taintAware.setTaint(b);
    }
}
