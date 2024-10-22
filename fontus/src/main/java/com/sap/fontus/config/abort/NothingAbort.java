package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class NothingAbort extends Abort {
    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        // Nothing to do here
        return taintAware;
    }

    @Override
    public String getName() {
        return "nothing";
    }

    static {
        Abort.add(new NothingAbort());
    }
}
