package com.sap.fontus.taintaware.lazycomplex.operations;

public class DeleteOperation extends RemoveOperation {
    public DeleteOperation(int start, int end) {
        super(start, end);
    }

    @Override
    protected int leftShift() {
        return this.end - this.start;
    }
}
