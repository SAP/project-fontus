package com.sap.fontus.taintaware;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASStringable;
import com.sap.fontus.taintaware.shared.IASTaintSource;

import java.io.Serializable;
import java.util.List;

public interface IASTaintAware extends Serializable {
    boolean isTainted();

    void setTaint(boolean taint);

    void setTaint(IASTaintSource source);

    void setContent(String content, List<IASTaintRange> taintRanges);

    IASStringable toIASString();

    List<IASTaintRange> getTaintRanges();
}
