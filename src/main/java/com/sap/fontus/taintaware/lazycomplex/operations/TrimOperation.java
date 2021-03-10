package com.sap.fontus.taintaware.lazycomplex.operations;

public class TrimOperation extends AbstractTrimOperation {
    public TrimOperation() {
        super(true, true);
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