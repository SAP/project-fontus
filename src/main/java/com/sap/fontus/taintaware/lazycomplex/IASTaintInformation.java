package com.sap.fontus.taintaware.lazycomplex;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.utils.stats.Statistics;
import com.sap.fontus.taintaware.lazycomplex.operations.BaseOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IASTaintInformation implements IASTaintInformationable {
    private String previousString;
    private IASTaintInformation previousInformation;
    private IASOperation operation;
    private volatile List<IASTaintRange> cache;

    public IASTaintInformation(String previousString, IASTaintInformation previousInformation, IASOperation operation) {
        this.previousString = previousString;
        this.previousInformation = previousInformation;
        this.operation = Objects.requireNonNull(operation);
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
    }

    public IASTaintInformation(BaseOperation operation) {
        this.previousString = null;
        this.previousInformation = null;
        this.operation = Objects.requireNonNull(operation);
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
    }

    public IASTaintInformation() {
        this.previousString = null;
        this.previousInformation = null;
        this.operation = new BaseOperation();
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
    }

    public IASTaintInformation(List<IASTaintRange> taintRanges) {
        this.previousString = null;
        this.previousInformation = null;
        this.operation = new BaseOperation(taintRanges);
        if (Configuration.getConfiguration().collectStats()) {
            Statistics.INSTANCE.incrementLazyTaintInformationCreated();
        }
    }

    public synchronized boolean isTainted() {
        List<IASTaintRange> ranges = this.evaluate();
        if (ranges.size() == 0) {
            return false;
        } else if (ranges.size() == 1) {
            IASTaintRange range = ranges.get(0);
            return !(range.getStart() == range.getEnd());
        } else {
            return true;
        }
    }

    @Override
    public synchronized IASTaintSource getTaintFor(int position) {
        return new IASTaintRanges(this.getTaintRanges()).getTaintFor(position);
    }

    @Override
    public synchronized boolean isTaintedAt(int index) {
        return new IASTaintRanges(this.getTaintRanges()).isTaintedAt(index);
    }

    private synchronized List<IASTaintRange> evaluate() {
        if (this.cache == null) {
            if (Configuration.getConfiguration().collectStats()) {
                Statistics.INSTANCE.incrementLazyTaintInformationEvaluated();
            }
            List<IASTaintRange> ranges = null;
            if (this.previousInformation != null) {
                ranges = new ArrayList<>(this.previousInformation.evaluate());
            }
            List<IASTaintRange> cache = this.operation.apply(this.previousString, ranges);
            IASTaintRangeUtils.merge(cache);
            cache = Collections.unmodifiableList(cache);

            if (Configuration.getConfiguration().useCaching()) {
                this.cache = cache;
                this.previousString = null;
                this.previousInformation = null;
                this.operation = null;
            }
            return cache;
        }
        return this.cache;
    }

    public synchronized List<IASTaintRange> getTaintRanges() {
        return new ArrayList<>(this.evaluate());
    }
}
