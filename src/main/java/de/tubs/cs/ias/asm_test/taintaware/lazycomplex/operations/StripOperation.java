package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;

public class StripOperation extends AbstractTrimOperation {
    public StripOperation(IASLazyComplexAware previous, boolean leading, boolean trailing) {
        super(previous, leading, trailing);
    }

    @Override
    protected boolean isWhitespace(int codePointAt) {
        return Character.isWhitespace(codePointAt);
    }
}
