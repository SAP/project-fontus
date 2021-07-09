package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.List;

import com.sap.fontus.sanitizer.Sanitization;

public class SanitizationAbort extends Abort {
    @Override
        String taintedString = taintAware.toIASString().getString();
    public void abort(IASTaintAware taintAware, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        List<IASTaintRange> ranges = taintAware.toIASString().getTaintRanges();
        // until categories are specified by the taint mechanism, use:
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\" of categories %s! \n", taintAware, sinkFunction, categories);
        // sanitize here
        String sanitizedString = Sanitization.sanitizeSinks(taintedString, ranges, categories);
        System.err.println(taintedString + " was sanitized and resulted in: " + sanitizedString);
        taintAware.setContent(sanitizedString, ranges);
    }

    @Override
    public String getName() {
        return "sanitization";
    }
}
