package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.range.IASString;
import de.tubs.cs.ias.asm_test.taintaware.range.IASStringBuffer;
import de.tubs.cs.ias.asm_test.taintaware.range.IASStringBuilder;

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

    public static void setTaint(IASString taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(IASStringBuilder taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(IASStringBuffer taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.bool.IASString taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.bool.IASStringBuilder taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.bool.IASStringBuffer taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.array.IASString taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.array.IASStringBuilder taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.array.IASStringBuffer taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASStringBuilder taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASStringBuffer taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static Boolean isTainted(IASString taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(IASStringBuffer taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(IASStringBuilder taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.bool.IASString taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.bool.IASStringBuffer taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.bool.IASStringBuilder taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.array.IASString taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.array.IASStringBuffer taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.array.IASStringBuilder taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASStringBuffer taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASStringBuilder taintAware) {
        return taintAware.isTainted();
    }
}
