package de.tubs.cs.ias.asm_test;

import de.tubs.cs.ias.asm_test.taintaware.IASString;
import de.tubs.cs.ias.asm_test.taintaware.IASStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"ClassIndependentOfModule", "ClassOnlyUsedInOneModule", "ClassUnconnectedToPackage", "ClassOnlyUsedInOnePackage"})
public class TaintStringUtilsTests {
    @Test
    public void testConcat() {
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
    public void testConcatNull() {
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
