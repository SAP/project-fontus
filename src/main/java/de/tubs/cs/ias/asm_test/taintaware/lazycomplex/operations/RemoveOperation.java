package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class RemoveOperation implements IASOperation {
    protected final int start;
    protected final int end;

    public RemoveOperation(int start, int end) {
        this.start = start;
        this.end = end;
    }

    protected abstract int leftShift();

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint) {
        List<IASTaintRange> head = new ArrayList<>(previousTaint);
        List<IASTaintRange> tail = new ArrayList<>(previousTaint);

        IASTaintRangeUtils.adjustRanges(head, 0, this.start, 0);
        IASTaintRangeUtils.adjustRanges(tail, this.end, Integer.MAX_VALUE, this.leftShift());
        head.addAll(tail);
        return head;
    }

}
