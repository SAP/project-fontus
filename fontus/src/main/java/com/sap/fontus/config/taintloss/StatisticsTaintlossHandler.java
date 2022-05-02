package com.sap.fontus.config.taintloss;

import com.sap.fontus.Constants;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.utils.stats.Statistics;

import java.util.List;

public class StatisticsTaintlossHandler extends TaintlossHandler {
    @Override
    protected void handleTaintlossInternal(IASTaintAware taintAware, List<StackTraceElement> stackTrace) {
        StackTraceElement callee = stackTrace.get(0);
        StackTraceElement caller = stackTrace.get(1);
        for (int i = 1; i < stackTrace.size(); i++) {
            caller = stackTrace.get(i);
            if (!caller.getClassName().startsWith(Constants.PACKAGE)) {
                break;
            }
        }
        String call = String.format("%s.%s -> %s.%s", caller.getClassName(), caller.getMethodName(), callee.getClassName(), callee.getMethodName());
        if (caller.getClassName().startsWith("java.lang.AbstractStringBuilder")) {
            System.out.println();
        }
        Statistics.INSTANCE.incrementTaintlossHits(call);
    }

    @Override
    public String getName() {
        return "statistics_logging";
    }
}
