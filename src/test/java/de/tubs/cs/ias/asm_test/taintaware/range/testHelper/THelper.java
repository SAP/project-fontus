package de.tubs.cs.ias.asm_test.taintaware.range.testHelper;

import de.tubs.cs.ias.asm_test.taintaware.range.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.range.IASRangeAware;

public class THelper {
    public static boolean isUninitialized(IASRangeAware s) {
        return s.isUninitialized();
    }

    public static IASTaintInformation get(IASRangeAware sB) {
        return sB.getTaintInformation();
    }
}
