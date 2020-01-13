package de.tubs.cs.ias.asm_test.taintaware.range.testHelper;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.range.IASString;
import de.tubs.cs.ias.asm_test.taintaware.range.IASStringBuffer;
import de.tubs.cs.ias.asm_test.taintaware.range.IASStringBuilder;

public class THelper {
    public static boolean isUninitialized(IASString s) {
        return s.isUninitialized();
    }

    public static IASTaintInformation get(IASStringBuffer sB) {
        return sB.getTaintInformation();
    }

    public static IASTaintInformation get(IASString foo) {
        return foo.getTaintInformation();
    }

    public static boolean isUninitialized(IASStringBuilder sB) {
        return sB.isUninitialized();
    }
}
