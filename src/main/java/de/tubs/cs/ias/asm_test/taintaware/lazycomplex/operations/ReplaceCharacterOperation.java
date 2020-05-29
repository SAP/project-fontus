package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASString;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRanges;

import java.util.List;

public class ReplaceCharacterOperation extends IASOperation {
    private final char toReplace;

    public ReplaceCharacterOperation(IASLazyComplexAware previous, char toReplace) {
        super(previous);
        this.toReplace = toReplace;
    }

    @Override
    public List<IASTaintRange> apply() {
        if (((IASString) this.previous).indexOf(this.toReplace) < 0) {
            return this.previous.getTaintRanges();
        }

        IASTaintRanges ti = new IASTaintRanges(this.previous.getTaintRanges());

        int index = ((IASString) this.previous).indexOf(this.toReplace);
        do {
            ti.removeTaintFor(index, index + 1, false);
        } while ((index = ((IASString) this.previous).indexOf(this.toReplace, index)) >= 0);

        return ti.getAllRanges();
    }
}
