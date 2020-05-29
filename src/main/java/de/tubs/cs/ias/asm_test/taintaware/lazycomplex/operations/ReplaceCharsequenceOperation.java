package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;

import java.util.List;

public class ReplaceCharSequenceOperation extends IASOperation {
    private final IASString toReplace;
    private final IASString replacement;

    public ReplaceCharSequenceOperation(IASLazyComplexAware previous, IASString toReplace, IASString replacement) {
        super(previous);
        this.toReplace = toReplace;
        this.replacement = replacement;
    }

    @Override
    public List<IASTaintRange> apply() {
        IASTaintRanges ranges = new IASTaintRanges(this.previous.getTaintRanges());
        if (((IASString) this.previous).indexOf(this.toReplace) < 0) {
            return this.previous.getTaintRanges();
        }

        int index = ((IASString) this.previous).indexOf(this.toReplace);
        do {
            ranges.replaceTaintInformation(index, index + this.toReplace.length(), this.replacement.getTaintRanges(), this.replacement.length(), true);
        } while ((index = ((IASString) this.previous).indexOf(this.toReplace, index)) >= 0);

        return ranges.getAllRanges();
    }
}
