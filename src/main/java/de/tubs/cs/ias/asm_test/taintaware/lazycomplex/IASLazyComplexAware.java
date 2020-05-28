package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.List;

public interface IASLazyComplexAware extends IASTaintAware {
    List<IASTaintRange> getTaintRanges();
    int length();
}
