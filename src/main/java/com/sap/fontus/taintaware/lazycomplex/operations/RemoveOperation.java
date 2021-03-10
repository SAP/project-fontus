package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;

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

        IASTaintRangeUtils.adjustAndRemoveRanges(head, 0, this.start, 0);
        IASTaintRangeUtils.adjustAndRemoveRanges(tail, this.end, Integer.MAX_VALUE, this.leftShift());
        head.addAll(tail);
        return head;
    }

}
