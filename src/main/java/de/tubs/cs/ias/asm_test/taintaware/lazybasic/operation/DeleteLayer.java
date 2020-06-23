package de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASLayer;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public class DeleteLayer extends IASLayer {
    public DeleteLayer(int start, int end) {
        super(start, end);
    }

    public DeleteLayer(int start) {
        this(start, Integer.MAX_VALUE);
    }

    public DeleteLayer() {
        this(0);
    }

    @Override
    protected List<IASTaintRange> apply(List<IASTaintRange> previousRanges) {
        List<IASTaintRange> before = new ArrayList<>(previousRanges);
        List<IASTaintRange> after = new ArrayList<>(previousRanges);
        IASTaintRangeUtils.adjustAndRemoveRanges(before, 0, start, 0);
        IASTaintRangeUtils.adjustAndRemoveRanges(after, end, Integer.MAX_VALUE, end - start);
        List<IASTaintRange> taintRanges = new ArrayList<>(before.size() + after.size());
        taintRanges.addAll(before);
        taintRanges.addAll(after);
        IASTaintRangeUtils.merge(taintRanges);
        return taintRanges;
    }
}
