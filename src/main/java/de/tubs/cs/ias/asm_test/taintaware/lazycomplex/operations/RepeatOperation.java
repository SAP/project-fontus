package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public class RepeatOperation implements IASOperation {
    private final int count;

    public RepeatOperation(int count) {
        this.count = count;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        List<IASTaintRange> ranges = new ArrayList<>(previousRanges);
        List<IASTaintRange> newRanges = new ArrayList<>(ranges.size() * this.count);
        for (int i = 0; i < count; i++) {
            ranges = new ArrayList<>(previousRanges);
            IASTaintRangeUtils.shiftRight(ranges, i * previousString.length());
            newRanges.addAll(ranges);
        }
        return newRanges;
    }
}
