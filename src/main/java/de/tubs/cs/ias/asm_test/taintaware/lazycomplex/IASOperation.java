package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.List;

public interface IASOperation {
    List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint);
}
