package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.lazybasic.IASLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
