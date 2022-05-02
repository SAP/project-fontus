package com.sap.fontus.config.taintloss;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

import static com.sap.fontus.utils.Utils.convertStackTrace;

public abstract class LoggingTaintlossHandler extends TaintlossHandler {
    protected final String format(IASTaintAware taintAware, List<StackTraceElement> stackTrace) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Taintlossy method hit on string \"%s\"!\n", taintAware));
        List<String> stackTraceStrings = convertStackTrace(stackTrace);
        for (String ste : stackTraceStrings) {
            stringBuilder.append("\tat ").append(ste).append('\n');
        }
        return stringBuilder.toString();
    }
}
