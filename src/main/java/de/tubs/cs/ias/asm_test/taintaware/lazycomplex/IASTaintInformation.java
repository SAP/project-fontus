package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import java.util.List;
import java.util.Objects;

public class IASTaintInformation {
    private IASOperation operation;

    public IASTaintInformation(IASOperation operation) {
        this.operation = Objects.requireNonNull(operation);
    }

    public boolean isTainted() {
        return this.evaluate().size() > 0;
    }

    public List<IASTaintRange> evaluate() {
        return operation.apply();
    }
}
