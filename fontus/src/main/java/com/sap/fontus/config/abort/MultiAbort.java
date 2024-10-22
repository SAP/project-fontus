package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class MultiAbort extends Abort {

    private final List<Abort> aborts;

    public MultiAbort(List<Abort> aborts) {
        this.aborts = aborts;
    }

    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        for (Abort a : this.aborts) {
            if (a != null) {
                taintAware = a.abort(taintAware, instance, sinkFunction, sinkName, stackTrace);
            }
        }
        return taintAware;
    }

    @Override
    public String getName() {
        return "multi";
    }

}
