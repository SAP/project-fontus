package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.lazybasic.IASTaintInformation;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.lazybasic.IASLayer;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public class InsertLayer extends IASLayer {
    private IASTaintInformation incomingTaintInformation;

    public InsertLayer(int start, int end, IASTaintInformation incomingTaintInformation) {
        super(start, end);
        this.incomingTaintInformation = incomingTaintInformation;
    }

    public InsertLayer(int start, int end, IASTaintSource taintSource) {
        super(start, end);
        this.incomingTaintInformation = new IASTaintInformation(new BaseLayer(0, end - start, taintSource));
    }

    public InsertLayer(int start, int end) {
        super(start, end);
        this.incomingTaintInformation = new IASTaintInformation();
    }

    private List<IASTaintRange> getIncomingTaint() {
        if(this.incomingTaintInformation == null) {
            return new ArrayList<>(0);
        }
        return this.incomingTaintInformation.getTaintRanges();
    }

    @Override
    protected List<IASTaintRange> apply(List<IASTaintRange> previousRanges) {
        List<IASTaintRange> before = new ArrayList<>(previousRanges);
        List<IASTaintRange> after = new ArrayList<>(previousRanges);
        List<IASTaintRange> insertion = this.getIncomingTaint();

        IASTaintRangeUtils.adjustAndRemoveRanges(before, 0, start, 0);
        IASTaintRangeUtils.adjustAndRemoveRanges(after, start, Integer.MAX_VALUE, start - end);
        IASTaintRangeUtils.adjustAndRemoveRanges(insertion, 0, end - start, -start);

        List<IASTaintRange> result = new ArrayList<>(before.size() + insertion.size() + after.size());
        result.addAll(before);
        result.addAll(insertion);
        result.addAll(after);
        IASTaintRangeUtils.merge(result);

        return result;
    }
}
