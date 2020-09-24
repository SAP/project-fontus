package de.tubs.cs.ias.asm_test.utils.abort;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.util.List;

public class NothingAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware, String sink, String category, List<StackTraceElement> stackTrace) {
        // Nothing to do here
    }

    @Override
    public String getName() {
        return "nothing";
    }
}
