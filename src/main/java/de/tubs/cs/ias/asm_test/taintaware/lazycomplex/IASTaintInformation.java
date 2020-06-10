package de.tubs.cs.ias.asm_test.taintaware.lazycomplex;

import de.tubs.cs.ias.asm_test.taintaware.lazycomplex.operations.BaseOperation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IASTaintInformation {
    private String previousString;
    private IASTaintInformation previousInformation;
    private IASOperation operation;
    private volatile List<IASTaintRange> cache;

    public IASTaintInformation(String previousString, IASTaintInformation previousInformation, IASOperation operation) {
        this.previousString = previousString;
        this.previousInformation = previousInformation;
        this.operation = Objects.requireNonNull(operation);
    }

    public IASTaintInformation(BaseOperation operation) {
        this.previousString = null;
        this.previousInformation = null;
        this.operation = operation;
    }

    public IASTaintInformation() {
        this.previousString = null;
        this.previousInformation = null;
        this.operation = new BaseOperation();
    }

    public IASTaintInformation(List<IASTaintRange> taintRanges) {
        this.previousString = null;
        this.previousInformation = null;
        this.operation = new BaseOperation(taintRanges);
    }

    public synchronized boolean isTainted() {
        return this.evaluate().size() > 0;
    }

    public synchronized List<IASTaintRange> evaluate() {
        if (this.cache == null) {
            List<IASTaintRange> ranges = null;
            if(this.previousInformation != null) {
                ranges = new ArrayList<>(this.previousInformation.evaluate());
            }
            this.cache = new ArrayList<>(this.operation.apply(this.previousString, ranges));
//            this.previousString = null;
//            this.previousInformation = null;
//            this.operation = null;
        }
        return this.cache;
    }
}
