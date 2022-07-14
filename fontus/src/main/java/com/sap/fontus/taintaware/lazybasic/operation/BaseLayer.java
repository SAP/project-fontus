package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.lazybasic.IASLayer;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRangeUtils;
import com.sap.fontus.taintaware.shared.IASTaintRanges;

import java.util.Arrays;

public class BaseLayer implements IASLayer {
    private final IASTaintRanges base;

    public BaseLayer(IASTaintRanges base) {
        this.base = base.copy();
    }

    public BaseLayer(int size) {
        this(new IASTaintRanges(size));
    }

    public BaseLayer(int start, int end, IASTaintMetadata data) {
        this(new IASTaintRanges(end, Arrays.asList(new IASTaintRange(start, end, data))));
    }

    public IASTaintRanges getBase() {
        return base.copy();
    }

    @Override
    public IASTaintRanges apply(IASTaintRanges previousRanges) {
        return this.base.copy();
    }

    @Override
    public String toString() {
        return "BaseLayer: " + IASTaintRangeUtils.taintRangesAsString(base);
    }

}
