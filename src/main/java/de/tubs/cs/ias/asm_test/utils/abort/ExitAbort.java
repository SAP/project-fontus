package de.tubs.cs.ias.asm_test.utils.abort;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public class ExitAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware) {
        System.err.printf("String %s is tainted!\nAborting..!\n", taintAware);
        System.exit(1);
    }

    @Override
    public String getName() {
        return "exit";
    }
}
