package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.List;
import java.util.Objects;

public class ConcatOperation extends IASOperation {
    private final IASLazyComplexAware second;

    protected ConcatOperation(IASLazyComplexAware previous, IASLazyComplexAware second) {
        super(Objects.requireNonNull(previous));
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public List<IASTaintRange> apply() {
        List<IASTaintRange> firstTaint = this.previous.getTaintRanges();
        List<IASTaintRange> secondTaint = this.second.getTaintRanges();
        IASTaintRangeUtils.shiftRight(secondTaint, this.previous.length());
        firstTaint.addAll(secondTaint);
        return firstTaint;
    }
}
