package com.sap.fontus.taintaware.lazycomplex.operations;

public class StripOperation extends AbstractTrimOperation {
    public StripOperation(boolean leading, boolean trailing) {
        super(leading, trailing);
    }

    @Override
    protected boolean isWhitespace(int codePointAt) {
        return Character.isWhitespace(codePointAt);
    }
}
