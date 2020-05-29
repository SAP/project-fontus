package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASLazyComplexAware;

public class TrimOperation extends AbstractTrimOperation {
    public TrimOperation(IASLazyComplexAware previous) {
        super(previous, true, true);
    }

    @Override
    protected boolean isWhitespace(int codePointAt) {
        char[] chars = Character.toChars(codePointAt);
        if (chars.length == 1) {
            return chars[0] == ' ';
        }
        return false;
    }
}