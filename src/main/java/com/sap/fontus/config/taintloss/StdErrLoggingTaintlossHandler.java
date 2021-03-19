package com.sap.fontus.config.taintloss;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

import static com.sap.fontus.utils.Utils.convertStackTrace;

public class StdErrLoggingTaintlossHandler extends TaintlossHandler {
    @Override
    protected void handleTaintlossInternal(IASTaintAware taintAware, List<StackTraceElement> stackTrace) {
        System.err.printf("Taintlossy method hit on string \"%s\"!\n", taintAware);
        List<String> stackTraceStrings = convertStackTrace(stackTrace);
        for (String ste : stackTraceStrings) {
            System.err.println("\tat " + ste);
        }
    }

    @Override
    public String getName() {
        return "stderr_logging";
    }
}
