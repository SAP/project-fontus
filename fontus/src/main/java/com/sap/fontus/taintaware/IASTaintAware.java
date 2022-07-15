package com.sap.fontus.taintaware;

import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.io.Serializable;

public interface IASTaintAware extends Serializable {
    boolean isTainted();

    boolean isTaintedAt(int index);

    void setTaint(boolean taint);

    void setTaint(IASTaintMetadata data);

    IASTaintInformationable getTaintInformation();

    IASTaintInformationable getTaintInformationInitialized();

    void setContent(String content, IASTaintInformationable taintInformation);

    IASString toIASString();

    boolean isInitialized();

    default boolean isUninitialized() {
        return !this.isInitialized();
    }

    void initialize();

    IASTaintAware copy();

    IASTaintAware newInstance();

}
