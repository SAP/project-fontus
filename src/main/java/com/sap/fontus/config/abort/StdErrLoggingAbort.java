package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

import static com.sap.fontus.utils.Utils.convertStackTrace;

public class StdErrLoggingAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\" of category \"%s\"! \n", taintAware, sinkFunction, sinkName);
        List<String> stackTraceStrings = convertStackTrace(stackTrace);
        for (String ste: stackTraceStrings) {
            System.err.println("\tat " + ste);
        }
    }

    @Override
    public String getName() {
        return "stderr_logging";
    }
}
