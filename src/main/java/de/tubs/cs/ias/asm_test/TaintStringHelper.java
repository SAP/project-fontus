package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.IASString;
import de.tubs.cs.ias.asm_test.taintaware.IASStringBuffer;
import de.tubs.cs.ias.asm_test.taintaware.IASStringBuilder;

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

    public static Boolean isTainted(IASString str) {
        return str.isTainted();
    }

    public static void setTaint(IASString str, Boolean b) {
        str.setTaint(b);
    }

    public static Boolean isTainted(IASStringBuffer str) {
        return str.isTainted();
    }

    public static void setTaint(IASStringBuffer str, Boolean b) {
        str.setTaint(b);
    }

    public static Boolean isTainted(IASStringBuilder str) {
        return str.isTainted();
    }

    public static void setTaint(IASStringBuilder str, Boolean b) {
        str.setTaint(b);
    }

    public static String getString(IASString str) {
        return str.getString();
    }
}
