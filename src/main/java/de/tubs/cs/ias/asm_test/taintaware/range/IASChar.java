package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public class IASChar implements IASTaintAware {
    private final char c;
    private IASTaintSource taintSource;

    public IASChar(char c, IASTaintSource taintSource) {
        this.c = c;
        this.taintSource = taintSource;
    }

    public IASChar(char c) {
        this(c, null);
    }

    @Override
    public boolean isTainted() {
        return this.taintSource != null;
    }

    @Override
    public void setTaint(boolean taint) {
        if(!isTainted() && taint) {
            this.taintSource = IASTaintSource.TS_CHAR_UNKNOWN_ORIGIN;
        } else if(!taint) {
            this.taintSource = null;
        }
    }

    public char getChar() {
        return c;
    }
}
