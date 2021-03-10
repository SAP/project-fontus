package com.sap.fontus;

import com.sap.fontus.taintaware.bool.IASString;
import com.sap.fontus.taintaware.bool.IASStringBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ClassIndependentOfModule", "ClassOnlyUsedInOneModule", "ClassUnconnectedToPackage", "ClassOnlyUsedInOnePackage", "DuplicateStringLiteralInspection", "TypeMayBeWeakened"})
class TaintAwareStringBuilderTests {
    @Test
    void emptyStringBuilderIsUntainted() {
        IASStringBuilder sb = new IASStringBuilder();
        assertFalse(sb.isTainted(), "If we construct a taint-aware StringBuilder without setting a taint, it should not be tainted.");
    }

    @Test
    void stringBuilderFromUntaintedString() {
        IASString s = new IASString("hello");
        IASStringBuilder sb = new IASStringBuilder(s);
        assertFalse(sb.isTainted(), "If we construct a taint-aware StringBuilder from an untainted String, it should not be tainted.");
    }


    @Test
    void stringBuilderFromTaintedString() {
        IASString s = new IASString("hello", true);
        IASStringBuilder sb = new IASStringBuilder(s);
        assertTrue(sb.isTainted(), "If we construct a taint-aware StringBuilder from a tainted String, it should be tainted.");
    }

    @Test
    void appendUntaintedString() {
        IASString s = new IASString("hello");
        IASStringBuilder sb = new IASStringBuilder();
        sb.append(s);
        assertFalse(sb.isTainted(), "If we append an untainted String to an untainted StringBuilder, it should not be tainted.");
    }

    @Test
    void appendTaintedString() {
        IASString s = new IASString("hello", true);
        IASStringBuilder sb = new IASStringBuilder();
        sb.append(s);
        assertTrue(sb.isTainted(), "If we append a tainted String to an untainted StringBuilder, it should not be tainted.");
    }
}
