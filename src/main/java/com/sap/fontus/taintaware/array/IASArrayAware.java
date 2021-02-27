package com.sap.fontus.taintaware.array;

import com.sap.fontus.taintaware.shared.IASTaintRangeAware;

public interface IASArrayAware extends IASTaintRangeAware {
    int[] getTaints();
}
