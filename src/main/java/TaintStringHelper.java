package de.tubs.cs.ias.asm_test;

public class TaintStringHelper {

    public static Boolean isTainted(String str) {
	throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    public static void setTaint(String str, Boolean b) {
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

    public static String getString(IASString str) {
	return str.getString();
    }
}
