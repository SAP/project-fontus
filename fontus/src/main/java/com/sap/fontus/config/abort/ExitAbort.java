package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class ExitAbort extends Abort {
    private final StdErrLoggingAbort stdErrLoggingAbort = new StdErrLoggingAbort();
    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        this.stdErrLoggingAbort.abort(taintAware, instance, sinkFunction, sinkName, stackTrace);
        System.exit(1);
        return taintAware;
    }

    @Override
    public String getName() {
        return "exit";
    }

    static {
        Abort.add(new ExitAbort());
    }
}
