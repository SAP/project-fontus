package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public class SubstringOperation extends IASOperation {
    private final int start;
    private final int end;

    public SubstringOperation(IASLazyComplexAware previous, int start) {
        this(previous, start, previous.length());
    }

    public SubstringOperation(IASLazyComplexAware previous, int start, int end) {
        super(previous);
        this.start = start;
        this.end = end;
    }

    @Override
    public List<IASTaintRange> apply() {
        List<IASTaintRange> ranges = new ArrayList<>(this.previous.getTaintRanges());
        IASTaintRangeUtils.adjustRanges(ranges, this.start, this.end, this.start);
        return ranges;
    }
}
