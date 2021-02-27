package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;

import java.util.List;

public class ReplaceCharacterOperation implements IASOperation {
    private final char toReplace;

    public ReplaceCharacterOperation(char toReplace) {
        this.toReplace = toReplace;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        // TODO How to replace?
        return previousRanges;
//        if (previousString.indexOf(this.toReplace) < 0) {
//            return previousRanges;
//        }
//
//        IASTaintRanges ti = new IASTaintRanges(previousRanges);
//
//        int index = previousString.indexOf(this.toReplace);
//        do {
//            ti.removeTaintFor(index, index + 1, false);
//        } while ((index = previousString.indexOf(this.toReplace, index + 1)) >= 0);
//
//        return ti.getAllRanges();
    }
}
