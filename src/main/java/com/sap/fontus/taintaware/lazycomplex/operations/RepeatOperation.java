package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;

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
