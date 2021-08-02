package com.sap.fontus;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import com.sap.fontus.taintaware.unified.IASStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"ClassUnconnectedToPackage", "ClassOnlyUsedInOnePackage"})
public class TaintStringHelper {

    public static char[] getTaintMethodName() {
        return Configuration.getConfiguration().getTaintMethod().getName().toCharArray();
    }

    public static void setCaching(boolean enabled) {
        Configuration.getConfiguration().setUseCaching(enabled);
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
            ranges.add(new IASTaintRange(start, end, (i + 1)));
        }

        return ranges;
    }

    public static void setTaintRanges(IASString s, int count) {
        s.setTaint(count > 0);
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

    public static Boolean isTainted(IASString taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(IASStringBuilder taintAware) {
        return taintAware.isTainted();
    }

    public static Boolean isTainted(IASStringBuffer taintAware) {
        return taintAware.isTainted();
    }
}
