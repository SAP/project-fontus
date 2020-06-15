package de.tubs.cs.ias.asm_test.taintaware.range.testHelper;

import de.tubs.cs.ias.asm_test.taintaware.range.IASExtendedTaintRangeAware;
import de.tubs.cs.ias.asm_test.taintaware.range.IASTaintInformation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeAware;

@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class THelper {
    public static boolean isUninitialized(IASTaintRangeAware s) {
        return s.isUninitialized();
    }

    public static IASTaintInformation get(IASExtendedTaintRangeAware sB) {
        sB.initialize();
        return sB.getTaintInformation();
    }
}
