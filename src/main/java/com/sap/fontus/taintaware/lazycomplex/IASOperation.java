package com.sap.fontus.taintaware.lazycomplex;

import com.sap.fontus.taintaware.shared.IASTaintRange;

import java.io.Serializable;
import java.util.List;

public interface IASOperation extends Serializable {
    List<IASTaintRange> apply(String previousString, List<IASTaintRange> previousTaint);
}
