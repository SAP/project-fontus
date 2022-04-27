package com.sap.fontus.config.taintloss;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class StdErrLoggingTaintlossHandler extends LoggingTaintlossHandler {
    @Override
    protected void handleTaintlossInternal(IASTaintAware taintAware, List<StackTraceElement> stackTrace) {
        System.err.println(this.format(taintAware, stackTrace));
    }

    @Override
    public String getName() {
        return "stderr_logging";
    }
}
