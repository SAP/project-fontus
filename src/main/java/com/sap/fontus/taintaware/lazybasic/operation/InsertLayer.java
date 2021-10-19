package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.lazybasic.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.lazybasic.IASLayer;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public class InsertLayer implements IASLayer {
    private final IASTaintInformation incomingTaintInformation;
    private final int offset;

    public InsertLayer(int offset, IASTaintInformation incomingTaintInformation) {
        this.offset = offset;
        this.incomingTaintInformation = incomingTaintInformation;
    }

    private IASTaintRanges getIncomingTaint() {
        if(this.incomingTaintInformation == null) {
            return new IASTaintRanges(0);
        }
        return this.incomingTaintInformation.getTaintRanges();
    }

    public IASTaintRanges apply(IASTaintRanges ranges) {
        IASTaintRanges copied = ranges.copy();
        IASTaintRanges incomingTaint = this.getIncomingTaint();
        if (incomingTaint != null) {
            copied.insertTaint(this.offset, incomingTaint);
        }
        return copied;
    }

    @Override
    public String toString() {
        return "InsertLayer: " + offset + " length: " +
            incomingTaintInformation.getLength();
    }

}
