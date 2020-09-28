package de.tubs.cs.ias.asm_test.config.abort;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.util.List;

import static de.tubs.cs.ias.asm_test.utils.Utils.convertStackTrace;

public class StdErrLoggingAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware, String sink, String category, List<StackTraceElement> stackTrace) {
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\" of category \"%s\"! \n", taintAware, sink, category);
        List<String> stackTraceStrings = convertStackTrace(stackTrace);
        for (String ste: stackTraceStrings) {
            System.err.println("\tat " + ste);
        }
    }

    @Override
    public String getName() {
        return "stderr_logging";
    }
}
