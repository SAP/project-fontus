package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeAware;

public interface IASLazyComplexAware extends IASTaintRangeAware {
    int length();
    IASString toIASString();
}
