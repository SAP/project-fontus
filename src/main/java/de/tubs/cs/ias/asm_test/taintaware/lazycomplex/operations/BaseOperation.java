package de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.IASOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.List;

public class BaseOperation implements IASOperation {
    private final List<IASTaintRange> ranges;

    public BaseOperation() {
        this.ranges = new ArrayList<>(0);
    }

    public BaseOperation(List<IASTaintRange> ranges) {
        this.ranges = ranges;
    }

    @Override
    public List<IASTaintRange> apply(String before, List<IASTaintRange> beforeTaintRange) {
        return new ArrayList<>(ranges);
    }
}
