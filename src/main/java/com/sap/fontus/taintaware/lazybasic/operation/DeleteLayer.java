package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.lazybasic.IASLayer;
import com.sap.fontus.taintaware.shared.IASTaintRanges;

public class DeleteLayer implements IASLayer {
    protected final int start;
    protected final Integer end;

    public DeleteLayer(int start, int end) {
        this.checkBounds(start, end);
        this.start = start;
        this.end = end;
    }

    public DeleteLayer(int start) {
        this.start = start;
        this.end = null;
    }

    protected void checkBounds(int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start cannot be smaller than 0. start was " + start);
        }
        if (end < start) {
            throw new IllegalArgumentException("end cannot be smaller than start. start: " + start + " end: " + end);
        }
    }

    @Override
    public IASTaintRanges apply(IASTaintRanges ranges) {
        IASTaintRanges copied = ranges.copy();
        int end = this.end == null ? copied.getLength() : this.end;
        copied.delete(this.start, end, true);
        return copied;
    }

    @Override
    public String toString() {
        return "DeleteLayer: " + start + " end: " + end;
    }
}
