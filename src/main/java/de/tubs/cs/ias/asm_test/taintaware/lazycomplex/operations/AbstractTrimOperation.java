package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTrimOperation extends IASOperation {
    private final boolean leading;
    private final boolean trailing;

    public AbstractTrimOperation(IASLazyComplexAware previous, boolean leading, boolean trailing) {
        super(previous);
        this.leading = leading;
        this.trailing = trailing;
    }

    @Override
    public List<IASTaintRange> apply() {
        String s = this.previous.toString();

        int start = 0;
        if (this.leading) {
            for (int i = 0; i < s.length() && this.isWhitespace(s.codePointAt(i)); i++) {
                start = i;
            }
        }

        int end = s.length();
        if (this.trailing) {
            for (int i = s.length() - 1; i >= 0 && this.isWhitespace(s.codePointAt(i)); i--) {
                end = i;
            }
        }

        if (start < end) {
            List<IASTaintRange> ranges = new ArrayList<>(this.previous.getTaintRanges());
            IASTaintRangeUtils.adjustRanges(ranges, start, end, start);
            return ranges;
        } else {
            return new ArrayList<>(0);
        }
    }

    protected abstract boolean isWhitespace(int codePointAt);
}
