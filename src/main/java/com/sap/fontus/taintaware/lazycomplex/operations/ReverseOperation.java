package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.shared.IASTaintRanges;

import java.util.List;

public class ReverseOperation implements IASOperation {

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint) {
        IASTaintRanges ti = new IASTaintRanges(previousTaint);
        ti.reversed(previousString.length());
        return ti.getTaintRanges();
    }
}
