package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;

import java.util.ArrayList;
import java.util.List;

public class BaseOperation implements IASOperation {
    private final List<IASTaintRange> ranges;

    public BaseOperation() {
        this.ranges = new ArrayList<>(0);
    }

    public BaseOperation(List<IASTaintRange> ranges) {
        this.ranges = ranges;
    }

    public BaseOperation(int start, int end, IASTaintSource source) {
        boolean hasElement = start != end;
        this.ranges = new ArrayList<>(hasElement ? 1 : 0);
        if (hasElement) {
            this.ranges.add(new IASTaintRange(start, end, source));
        }
    }

    @Override
    public List<IASTaintRange> apply(String before, List<IASTaintRange> beforeTaintRange) {
        return new ArrayList<>(ranges);
    }
}
