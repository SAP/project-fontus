package de.tubs.cs.ias.asm_test.taintaware.lazybasic;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.List;

public abstract class IASLayer {
    protected final int start;
    protected final int end;

    protected IASLayer(int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start cannot be smaller than 0. start was " + start);
        }
        if (end < start) {
            throw new IllegalArgumentException("end cannot be smaller than start. start: " + start + " end: " + end);
        }
        this.start = start;
        this.end = end;
    }

    protected abstract List<IASTaintRange> apply(List<IASTaintRange> previousRanges);
}
