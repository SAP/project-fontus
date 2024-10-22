package com.sap.fontus.config.taintloss;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class TaintlossHandler {
    private static final ConcurrentHashMap<String, TaintlossHandler> handlers = new ConcurrentHashMap<>(10);

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
     * @param name the TaintlossHandler name. Comparison is case-insensitive
     * @return the TaintlossHandler object
     * @throws IllegalArgumentException If no corresponding Taintloss handler was found
     */
    public static TaintlossHandler parse(String name) {
        TaintlossHandler handler = handlers.get(name);
        if(handler == null) {
            throw new IllegalArgumentException(String.format("Taintloss handler with name \"%s\" not found! Please use on of: [%s]", name, handlers.values().stream().map(TaintlossHandler::getName).map(n -> String.format("\"%s\"", n)).collect(Collectors.joining(","))));
        }
        return handler;
    }

    public static void add(TaintlossHandler handler) {
        handlers.put(handler.getName().toLowerCase(), handler);
    }

    public static void logTaintloss(IASTaintAware taintAware) {
        if (Configuration.getConfiguration().handleTaintloss()) {
            Configuration.getConfiguration().getTaintlossHandler().handleTaintloss(taintAware);
        }
    }
}
