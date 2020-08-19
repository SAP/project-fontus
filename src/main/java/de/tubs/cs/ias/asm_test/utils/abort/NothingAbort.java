package de.tubs.cs.ias.asm_test.utils.abort;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

public class NothingAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware) {
        // Nothing to do here
    }

    @Override
    public String getName() {
        return "nothing";
    }
}
