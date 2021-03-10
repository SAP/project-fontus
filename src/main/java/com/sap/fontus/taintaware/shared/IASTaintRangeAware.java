package com.sap.fontus.taintaware.shared;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public interface IASTaintRangeAware extends IASTaintAware {

    boolean isUninitialized();

    default boolean isInitialized() {
        return !isUninitialized();
    }

    void initialize();

    boolean isTaintedAt(int index);

    void setTaint(List<IASTaintRange> ranges);

    IASTaintInformationable getTaintInformation();
}
