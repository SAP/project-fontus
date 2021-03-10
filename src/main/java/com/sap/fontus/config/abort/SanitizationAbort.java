package com.sap.fontus.config.abort;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.fontus.sanitizer.Sanitization;

public class SanitizationAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware, String sink, String category, List<StackTraceElement> stackTrace) {
        String taintedString = taintAware.toIASString().getString();
        List<IASTaintRange> ranges = taintAware.toIASString().getTaintRanges();
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\" of category \"%s\"! \n", taintAware, sink, category);
        // until categories are specified by the taint mechanism, use:
        List<Sanitization.AttackCategory> categories = new ArrayList();
        categories.add(Sanitization.AttackCategory.XSS);
        // sanitize here
        String sanitizedString = Sanitization.sanitizeSinks(taintedString, ranges, categories);
        System.err.println(taintedString + " was sanitized and resultet in: " + sanitizedString);
        taintAware.setContent(sanitizedString, ranges);
    }

    @Override
    public String getName() {
        return "sanitization";
    }
}
