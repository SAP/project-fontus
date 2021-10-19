package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;

import java.util.List;

public abstract class Abort {
    private static final Abort[] aborts = {
            new NothingAbort(), new ExitAbort(), new StdErrLoggingAbort(), new JsonLoggingAbort(), new SanitizationAbort()
    };

    public abstract void abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace);

    public abstract String getName();

    /**
     * Parses the Abort name and returns the corresponding Abort object
     *
     * @param name the Abort name. case insensitive
     * @return the Abort object or null if no corresponding one was found
     */
    public static Abort parse(String name) {
        for (Abort abort : aborts) {
            if (abort.getName().toLowerCase().equals(name)) {
                return abort;
            }
        }
        return null;
    }
}
