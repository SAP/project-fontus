package com.sap.fontus;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.bool.IASString;
import com.sap.fontus.taintaware.shared.IASStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ClassIndependentOfModule", "ClassOnlyUsedInOneModule", "ClassUnconnectedToPackage", "ClassOnlyUsedInOnePackage"})
class TaintStringUtilsTests {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.BOOLEAN);
    }

    @Test
    void testConcat() {
        String s1 = "x";
        IASString is1 = new IASString(s1);
        String s2 = "12";
        IASString is2 = new IASString(s2);
        String manual = s1 + " = " + s2;
        String ifmt = "\u0001 = \u0001";
        String concatStr = IASStringUtils.concat(ifmt, is2, is1).getString(); // Reverse argument order due to stack machine
        assertEquals(manual, concatStr, "Manual concat and Tstring.concat should result in the same output");
    }

    @Test
    void testConcatNull() {
        String s1 = "x";
        IASString is1 = new IASString(s1);
        String s2 = null;
        IASString is2 = null;
        String manual = s1 + " = " + s2;
        String ifmt = "\u0001 = \u0001";
        String concatStr = IASStringUtils.concat(ifmt, is2, is1).getString(); // Reverse argument order due to stack machine
        assertEquals(manual, concatStr, "Manual concat and Tstring.concat should result in the same output when null is present");
    }
}
