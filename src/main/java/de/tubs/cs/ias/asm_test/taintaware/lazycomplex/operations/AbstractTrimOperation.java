package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTrimOperation implements IASOperation {
    private final boolean leading;
    private final boolean trailing;

    public AbstractTrimOperation(boolean leading, boolean trailing) {
        this.leading = leading;
        this.trailing = trailing;
    }

    @Override
    public List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousRanges) {
        int start = 0;
        if (this.leading) {
            for (int i = 0; i < previousString.length() && this.isWhitespace(previousString.codePointAt(i)); i++) {
                start = i + 1;
            }
        }

        int end = previousString.length();
        if (this.trailing) {
            for (int i = previousString.length() - 1; i >= 0 && this.isWhitespace(previousString.codePointAt(i)); i--) {
                end = i;
            }
        }

        if (start < end) {
            List<IASTaintRange> ranges = new ArrayList<>(previousRanges);
            IASTaintRangeUtils.adjustAndRemoveRanges(ranges, start, end, start);
            return ranges;
        } else {
            return new ArrayList<>(0);
        }
    }

    protected abstract boolean isWhitespace(int codePointAt);
}
