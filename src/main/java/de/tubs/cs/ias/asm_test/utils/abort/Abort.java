package de.tubs.cs.ias.asm_test.utils.abort;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public abstract class Abort {
    private static final Abort[] aborts = {
            new NothingAbort(), new ExitAbort(), new StdErrLoggingAbort()
    };

    public abstract void abort(IASTaintAware taintAware);

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
