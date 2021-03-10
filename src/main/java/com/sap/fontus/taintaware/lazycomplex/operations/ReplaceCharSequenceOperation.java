package com.sap.fontus.taintaware.lazycomplex.operations;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.lazycomplex.IASOperation;
import com.sap.fontus.taintaware.lazycomplex.IASString;
import com.sap.fontus.taintaware.shared.IASTaintRanges;

import java.util.List;

public class ReplaceCharSequenceOperation implements IASOperation {
    private final IASString toReplace;
    private final IASString replacement;

    public ReplaceCharSequenceOperation(IASString toReplace, IASString replacement) {
        this.toReplace = toReplace;
        this.replacement = replacement;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        IASTaintRanges ranges = new IASTaintRanges(previousRanges);
        if (!previousString.contains(this.toReplace.getString())) {
            return previousRanges;
        }

        int index = previousString.indexOf(this.toReplace.getString());
        do {
            ranges.replaceTaintInformation(index, index + this.toReplace.length(), this.replacement.getTaintRanges(), this.replacement.length(), true);
        } while ((index = previousString.indexOf(this.toReplace.getString(), index + 1)) >= 0);

        return ranges.getTaintRanges();
    }
}
