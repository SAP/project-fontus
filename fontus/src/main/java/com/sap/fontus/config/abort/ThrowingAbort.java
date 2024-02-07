package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASString;

import java.util.List;

public class ThrowingAbort extends Abort {

    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        StringBuilder sb = new StringBuilder();
        IASString s = taintAware.toIASString();
        sb.append("Taint Violation: String: \"").append(s.getString()).append("\" entered function: ").append(sinkFunction);
        throw new TaintViolationException(sb.toString());
    }

    @Override
    public String getName() {
        return "throw";
    }
}
