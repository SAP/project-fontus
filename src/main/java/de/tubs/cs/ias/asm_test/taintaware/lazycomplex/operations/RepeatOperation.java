package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public class RepeatOperation extends IASOperation {
    private final int count;

    public RepeatOperation(IASLazyComplexAware previous, int count) {
        super(previous);
        this.count = count;
    }

    @Override
    public List<IASTaintRange> apply() {
        List<IASTaintRange> ranges = new ArrayList<>(this.previous.getTaintRanges());
        List<IASTaintRange> newRanges = new ArrayList<>(ranges.size() * this.count);
        for (int i = 0; i < count; i++) {
            ranges = new ArrayList<>(this.previous.getTaintRanges());
            IASTaintRangeUtils.shiftRight(ranges, i * this.previous.length());
            newRanges.addAll(ranges);
        }
        return newRanges;
    }
}
