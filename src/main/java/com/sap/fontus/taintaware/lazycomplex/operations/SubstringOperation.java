package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;

import java.util.List;

public class SubstringOperation implements IASOperation {
    private final int start;
    private final int end;

    public SubstringOperation(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public SubstringOperation(int start) {
        this.start = start;
        this.end = Integer.MAX_VALUE;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        IASTaintRangeUtils.adjustAndRemoveRanges(previousRanges, this.start, this.end, this.start);
        return previousRanges;
    }
}
