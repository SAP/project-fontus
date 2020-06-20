package de.tubs.cs.ias.asm_test.taintaware.lazybasic.operation;

import de.tubs.cs.ias.asm_test.taintaware.lazybasic.IASLayer;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;

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
        IASTaintRanges trs = new IASTaintRanges(previousRanges);
        trs.removeTaintFor(start, end, true);
        List<IASTaintRange> taintRanges = trs.getAllRanges();
        IASTaintRangeUtils.merge(taintRanges);
        return taintRanges;
    }
}
