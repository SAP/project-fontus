package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

public class RemoveTaintOperation extends RemoveOperation {
    public RemoveTaintOperation(int start, int end) {
        super(start, end);
    }

    public RemoveTaintOperation(int index) {
        super(index, index + 1);
    }

    @Override
    protected int leftShift() {
        return 0;
    }
}
