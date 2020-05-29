package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IASTaintInformation {
    private IASOperation operation;
    private List<IASTaintRange> cache;

    public IASTaintInformation(IASOperation operation) {
        this.operation = Objects.requireNonNull(operation);
    }

    public boolean isTainted() {
        return this.evaluate().size() > 0;
    }

    public List<IASTaintRange> evaluate() {
        if (this.cache == null) {
            this.cache = new ArrayList<>(this.operation.apply());
        }
        return this.cache;
    }
}
