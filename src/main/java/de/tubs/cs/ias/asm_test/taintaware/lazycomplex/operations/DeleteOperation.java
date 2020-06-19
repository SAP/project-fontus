package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

public class DeleteOperation extends RemoveOperation {
    public DeleteOperation(int start, int end) {
        super(start, end);
    }

    @Override
    protected int leftShift() {
        return this.end - this.start;
    }
}
