package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.List;

public class SubstringOperation implements IASOperation {
    private final int start;
    private final int end;

    public SubstringOperation(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        IASTaintRangeUtils.adjustRanges(previousRanges, this.start, this.end, this.start);
        return previousRanges;
    }
}
