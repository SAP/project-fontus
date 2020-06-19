package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public class ReplaceOperation implements IASOperation {
    private final int start;
    private final int end;
    private final IASString replacement;

    public ReplaceOperation(int start, int end, IASString replacement) {
        this.start = start;
        this.end = end;
        this.replacement = replacement;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint) {
        List<IASTaintRange> begin = new ArrayList<>(previousTaint);
        List<IASTaintRange> replacementRanges = this.replacement.getTaintRanges();
        List<IASTaintRange> end = new ArrayList<>(previousTaint);


        IASTaintRangeUtils.adjustAndRemoveRanges(begin, 0, this.start, 0);
        IASTaintRangeUtils.shiftRight(replacementRanges, start);
        int leftShift = (this.end - this.start) - this.replacement.length();
        IASTaintRangeUtils.adjustAndRemoveRanges(end, this.end, Integer.MAX_VALUE, leftShift);

        begin.addAll(replacementRanges);
        begin.addAll(end);
        return begin;
    }
}
