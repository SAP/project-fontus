package com.sap.fontus.config.taintloss;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TaintlossHandler {
    private static final TaintlossHandler[] handlers = {
            new StdErrLoggingTaintlossHandler(), new FileLoggingTaintlossHandler(), new StatisticsTaintlossHandler()
    };

    public final void handleTaintloss(IASTaintAware taintAware) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        List<StackTraceElement> stackTrace = Arrays.stream(stack, 3, stack.length).collect(Collectors.toList());

        this.handleTaintlossInternal(taintAware, stackTrace);
    }

    protected abstract void handleTaintlossInternal(IASTaintAware taintAware, List<StackTraceElement> stackTrace);

    public abstract String getName();

    /**
     * Parses the TaintlossHandler name and returns the corresponding TaintlossHandler object
     *
     * @param name the TaintlossHandler name. case insensitive
     * @return the TaintlossHandler object
     * @throws IllegalArgumentException If no corresponding Taintloss handler was found
     */
    public static TaintlossHandler parse(String name) {
        for (TaintlossHandler handler : handlers) {
            if (handler.getName().toLowerCase().equals(name)) {
                return handler;
            }
        }
        throw new IllegalArgumentException(String.format("Taintloss handler with name \"%s\" not found! Please use on of: [%s]", name, Arrays.stream(handlers).map(TaintlossHandler::getName).map(n -> String.format("\"%s\"", n)).collect(Collectors.joining(","))));
    }

    public static void logTaintloss(IASTaintAware taintAware) {
        if (Configuration.getConfiguration().handleTaintloss()) {
            Configuration.getConfiguration().getTaintlossHandler().handleTaintloss(taintAware);
        }
    }
}
