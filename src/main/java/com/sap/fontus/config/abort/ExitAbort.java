package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class ExitAbort extends Abort {
    private StdErrLoggingAbort stdErrLoggingAbort = new StdErrLoggingAbort();
    @Override
    public void abort(IASTaintAware taintAware, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        stdErrLoggingAbort.abort(taintAware, sinkFunction, sinkName, stackTrace);
        System.exit(1);
    }

    @Override
    public String getName() {
        return "exit";
    }
}
