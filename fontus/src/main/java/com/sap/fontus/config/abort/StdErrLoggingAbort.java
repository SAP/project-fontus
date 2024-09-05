package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public class StdErrLoggingAbort extends Abort {
    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\" of category \"%s\"! \n", taintAware, sinkFunction, sinkName);
        // Utils.printStackTrace(stackTrace);
        return taintAware;
    }

    @Override
    public String getName() {
        return "stderr_logging";
    }

    static {
        Abort.add(new StdErrLoggingAbort());
    }
}
