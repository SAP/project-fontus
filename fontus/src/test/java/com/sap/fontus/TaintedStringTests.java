package com.sap.fontus;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"DuplicateStringLiteralInspection", "ClassIndependentOfModule", "ClassOnlyUsedInOneModule", "ClassUnconnectedToPackage", "ClassOnlyUsedInOnePackage"})
class TaintedStringTests {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    void testOpenOlatIssue() {
        IASString lhs = new IASString("K", true);
        IASString rhs = new IASString("D", true);
        IASString t1 = lhs.concat(new IASString(", "));
        IASString t2 = t1.concat(rhs);
        IASString[] args = {t2, new IASString("User profile and visiting card")};
        IASString result = IASStringUtils.concat("\u0001: \u0001", (Object[]) args);
        assertTrue(result.isTainted(), "Concatenation of two untainted String should be untainted");
    }

    @Test
    void regularlyCreatedStringIsUntainted() {
        IASString str = new IASString("hello");
        assertFalse(str.isTainted(), "If we construct a taint-aware string without setting a taint, it should not be tainted.");
    }

    @Test
    void concatUntainted() {
        IASString lhs = new IASString("hello ");
        IASString rhs = new IASString("world");
        IASString result = lhs.concat(rhs);
        assertFalse(result.isTainted(), "Concatenation of two untainted String should be untainted");
    }

    @Test
    void concatTaintedWithUntainted() {
        IASString lhs = new IASString("hello ", true);
        IASString rhs = new IASString("world");
        IASString result = lhs.concat(rhs);
        assertTrue(result.isTainted(), "Concatenation of a tainted with an untainted String should be tainted");
    }

    @Test
    void concatUntaintedWithTainted() {
        IASString lhs = new IASString("hello ", true);
        IASString rhs = new IASString("world");
        IASString result = lhs.concat(rhs);
        assertTrue(result.isTainted(), "Concatenation of an untainted with a tainted String should be tainted");
    }

    @Test
    void replaceFirstUntainted() {
        IASString base = new IASString("Hello welt");
        IASString regex = new IASString("welt");
        IASString replacement = new IASString("world");
        IASString result = base.replaceFirst(regex, replacement);
        assertFalse(result.isTainted(), "Replacing a part of an untainted string with an untainted string should not be tainted");
    }

    @Test
    void replaceFirstBaseTainted() {
        IASString base = new IASString("Hello welt", true);
        IASString regex = new IASString("welt");
        IASString replacement = new IASString("world");
        IASString result = base.replaceFirst(regex, replacement);
        assertTrue(result.isTainted(), "Replacing a part of an tainted string with an untainted string should be tainted");
    }

    @Test
    void replaceFirstReplacementTainted() {
        IASString base = new IASString("Hello welt");
        IASString regex = new IASString("welt");
        IASString replacement = new IASString("world", true);
        IASString result = base.replaceFirst(regex, replacement);
        assertTrue(result.isTainted(), "Replacing a part of an untainted string with a tainted string should be tainted");
    }

    @Test
    void replaceFirstReplacementTaintedButNoMatch() {
        IASString base = new IASString("Hello welt");
        IASString regex = new IASString("Welt");
        IASString replacement = new IASString("world", true);
        IASString result = base.replaceFirst(regex, replacement);
        assertFalse(result.isTainted(), "Trying to replace a part of an untainted string with a tainted string that does not match should not be tainted");
    }

    @Test
    void replaceAllUntainted() {
        IASString base = new IASString("Hello welt");
        IASString regex = new IASString("welt");
        IASString replacement = new IASString("world");
        IASString result = base.replaceAll(regex, replacement);
        assertFalse(result.isTainted(), "Replacing a part of an untainted string with an untainted string should not be tainted");
    }

    @Test
    void replaceAllBaseTainted() {
        IASString base = new IASString("Hello welt", true);
        IASString regex = new IASString("welt");
        IASString replacement = new IASString("world");
        IASString result = base.replaceAll(regex, replacement);
        assertTrue(result.isTainted(), "Replacing a part of an tainted string with an untainted string should be tainted");
    }

    @Test
    void replaceAllReplacementTainted() {
        IASString base = new IASString("Hello welt");
        IASString regex = new IASString("welt");
        IASString replacement = new IASString("world", true);
        IASString result = base.replaceAll(regex, replacement);
        assertTrue(result.isTainted(), "Replacing a part of an untainted string with a tainted string should be tainted");
    }

    @Test
    void replaceAllReplacementTaintedButNoMatch() {
        IASString base = new IASString("Hello welt");
        IASString regex = new IASString("Welt");
        IASString replacement = new IASString("world", true);
        IASString result = base.replaceAll(regex, replacement);
        assertFalse(result.isTainted(), "Trying to replace a part of an untainted string with a tainted string that does not match should not be tainted");
    }
}
