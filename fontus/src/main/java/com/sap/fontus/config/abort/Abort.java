package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Abort {
    private static final ConcurrentHashMap<String, Abort> aborts = new ConcurrentHashMap<>(10);


    public abstract IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace);

    public abstract String getName();

    /**
     * Parses the Abort name and returns the corresponding Abort object
     *
     * @param name the Abort name. Comparison is case-insensitive
     * @return the Abort object or null if no corresponding one was found
     */
    public static Abort parse(String name) {
        return aborts.get(name.toLowerCase());
    }

    public static void add(Abort abort) {
        aborts.put(abort.getName().toLowerCase(), abort);
    }
}
