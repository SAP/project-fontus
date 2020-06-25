package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.config.TaintMethodConfig;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.range.IASString;
import de.tubs.cs.ias.asm_test.taintaware.range.IASStringBuffer;
import de.tubs.cs.ias.asm_test.taintaware.range.IASStringBuilder;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"ClassUnconnectedToPackage", "ClassOnlyUsedInOnePackage"})
public class TaintStringHelper {

    public static char[] getTaintMethodName() {
        return TaintMethodConfig.getTaintMethod().getName().toCharArray();
    }

    public static void setCaching(boolean enabled) {
        de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASTaintInformation.USE_CACHING = enabled;
        de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASTaintInformation.USE_CACHING = enabled;
    }

    public static void setTaintRanges(String s, int count) {
        throw new IllegalStateException("Bytecode instrumentation not engaged");
    }

    private static List<IASTaintRange> randomTaintRanges(int stringSize, int count) {
        if (stringSize < count) {
            throw new IllegalArgumentException("String size cannot be smaller than taint range count. String size: " + stringSize + " taint range count: " + count);
        }

        if (count == 0) {
            return Collections.emptyList();
        }

        int width = stringSize / count;
        List<IASTaintRange> ranges = new ArrayList<>(count);

        for (int start = 0, end = width, i = 0; i < count; start += width, end += width, i++) {
            ranges.add(new IASTaintRange(start, end, (short) (i + 1)));
        }

        return ranges;
    }

    public static void setTaintRanges(de.tubs.cs.ias.asm_test.taintaware.range.IASString s, int count) {
        List<IASTaintRange> ranges = randomTaintRanges(s.length(), count);
        s.setTaint(ranges);
    }

    public static void setTaintRanges(de.tubs.cs.ias.asm_test.taintaware.array.IASString s, int count) {
        List<IASTaintRange> ranges = randomTaintRanges(s.length(), count);
        s.setTaint(ranges);
    }

    public static void setTaintRanges(de.tubs.cs.ias.asm_test.taintaware.bool.IASString s, int count) {
        s.setTaint(count > 0);
    }

    public static void setTaintRanges(de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASString s, int count) {
        List<IASTaintRange> ranges = randomTaintRanges(s.length(), count);
        s.setTaint(ranges);
    }

    public static void setTaintRanges(de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString s, int count) {
        List<IASTaintRange> ranges = randomTaintRanges(s.length(), count);
        s.setTaint(ranges);
    }

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

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASString taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASStringBuilder taintAware, Boolean b) {
        taintAware.setTaint(b);
    }

    public static void setTaint(de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASStringBuffer taintAware, Boolean b) {
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

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASString taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASStringBuffer taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASStringBuilder taintAware) {
        return taintAware.isTainted();
    }
}
