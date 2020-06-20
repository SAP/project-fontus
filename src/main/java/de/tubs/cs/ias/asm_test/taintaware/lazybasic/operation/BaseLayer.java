package de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASLayer;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseLayer extends IASLayer {
    private final List<IASTaintRange> base;
    public BaseLayer(List<IASTaintRange> base) {
        super(-1, -1);
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
