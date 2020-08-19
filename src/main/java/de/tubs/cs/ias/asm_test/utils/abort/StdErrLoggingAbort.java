package de.tubs.cs.ias.asm_test.utils.abort;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public class StdErrLoggingAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware) {
        System.err.printf("String %s is tainted! \n", taintAware);
        new Throwable().printStackTrace();
    }

    @Override
    public String getName() {
        return "stderr_logging";
    }
}
