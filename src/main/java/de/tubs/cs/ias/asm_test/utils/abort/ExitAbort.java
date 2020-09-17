package de.tubs.cs.ias.asm_test.utils.abort;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.util.List;

public class ExitAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware, String sink, List<StackTraceElement> stackTrace) {
        System.err.printf("String %s is tainted!\nAborting..!\n", taintAware);
        System.exit(1);
    }

    @Override
    public String getName() {
        return "exit";
    }
}
