package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.shared.IASTaintRangeAware;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;

import java.util.List;
import java.util.Objects;

public class ConcatOperation implements IASOperation {
    private final IASTaintRangeAware second;

    public ConcatOperation(IASTaintRangeAware second) {
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        List<IASTaintRange> secondTaint = this.second.getTaintRanges();
        IASTaintRangeUtils.shiftRight(secondTaint, previousString.length());
        previousRanges.addAll(secondTaint);
        return previousRanges;
    }
}
