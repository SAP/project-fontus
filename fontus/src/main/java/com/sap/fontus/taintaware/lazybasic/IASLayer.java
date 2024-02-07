package com.sap.fontus.taintaware.lazybasic;

import com.sap.fontus.taintaware.shared.IASTaintRanges;

import java.io.Serializable;

public interface IASLayer extends Serializable {
    IASTaintRanges apply(IASTaintRanges ranges);
}
