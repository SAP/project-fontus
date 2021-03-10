package com.sap.fontus.taintaware.lazybasic.operation;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.lazybasic.IASLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseLayer extends IASLayer {
    private final List<IASTaintRange> base;
    public BaseLayer(List<IASTaintRange> base) {
        super(0, 0);
        this.base = Collections.unmodifiableList(base);
    }

    public BaseLayer() {
        this(Collections.emptyList());
    }

    public BaseLayer(int start, int end, IASTaintSource taintSource) {
        this(Collections.singletonList(new IASTaintRange(start, end, taintSource)));
    }

    public List<IASTaintRange> getBase() {
        return base;
    }

    @Override
    protected List<IASTaintRange> apply(List<IASTaintRange> previousRanges) {
        return new ArrayList<>(base);
    }
}
