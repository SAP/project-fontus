package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class NothingAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        // Nothing to do here
    }

    @Override
    public String getName() {
        return "nothing";
    }
}
