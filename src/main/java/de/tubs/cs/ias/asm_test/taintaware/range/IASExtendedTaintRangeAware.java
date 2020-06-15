package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeAware;

public interface IASExtendedTaintRangeAware extends IASTaintRangeAware {
    IASTaintInformation getTaintInformation();
}
