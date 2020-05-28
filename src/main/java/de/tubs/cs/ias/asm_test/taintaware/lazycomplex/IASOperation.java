package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.List;

public abstract class IASOperation {
    protected IASLazyComplexAware previous;

    protected IASOperation(IASLazyComplexAware previous) {
        this.previous = previous;
    }

    public abstract List<IASTaintRange> apply();
}
