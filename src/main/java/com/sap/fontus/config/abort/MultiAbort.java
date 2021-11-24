package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class MultiAbort extends Abort {

    private List<Abort> aborts;

    public MultiAbort(List<Abort> aborts) {
        this.aborts = aborts;
    }

    @Override
    public void abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        for (Abort a : aborts) {
            a.abort(taintAware, instance, sinkFunction, sinkName, stackTrace);
        }
    }

    @Override
    public String getName() {
        return "multi";
    }
}
