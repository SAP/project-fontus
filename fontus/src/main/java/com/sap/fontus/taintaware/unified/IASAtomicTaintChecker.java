package com.sap.fontus.taintaware.unified;

import com.sap.fontus.taintaware.IASTaintAware;

public interface IASAtomicTaintChecker {

    IASTaintAware checkTaint(IASTaintAware object, Object instance, String sinkFunction, String sinkName, String callerFunction);

}
