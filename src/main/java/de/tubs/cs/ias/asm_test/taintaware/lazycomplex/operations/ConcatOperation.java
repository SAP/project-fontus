package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASLazyAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.List;
import java.util.Objects;

public class ConcatOperation implements IASOperation {
    private final IASLazyAware second;

    public ConcatOperation(IASLazyAware second) {
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        List<IASTaintRange> secondTaint = this.second.getTaintRanges();
        IASTaintRangeUtils.shiftRight(secondTaint, previousString.length());
        previousRanges.addAll(secondTaint);
        return previousRanges;
    }
}
