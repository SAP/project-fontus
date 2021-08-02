package com.sap.fontus.taintaware;

import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.unified.IASString;

import java.io.Serializable;

public interface IASTaintAware extends Serializable {
    boolean isTainted();

    boolean isTaintedAt(int index);

    void setTaint(boolean taint);

    void setTaint(IASTaintSource source);

    IASTaintInformationable getTaintInformation();

    IASTaintInformationable getTaintInformationInitialized();

    void setContent(String content, IASTaintInformationable taintInformation);

    IASString toIASString();

    boolean isInitialized();

    default boolean isUninitialized() {
        return !isInitialized();
    }

    void initialize();
}
