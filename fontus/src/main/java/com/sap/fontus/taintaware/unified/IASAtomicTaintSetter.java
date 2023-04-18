package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.IASTaintAware;

@FunctionalInterface
public interface IASAtomicTaintSetter {

    IASTaintAware setTaint(IASTaintAware object, Object parentObject, Object[] parameters, int sourceId, String callerFunction);

}
