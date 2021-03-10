package com.sap.fontus.config.abort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.range.IASString;
import com.sap.fontus.taintaware.shared.IASStringable;
import com.sap.fontus.taintaware.shared.IASTaintRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sap.fontus.sanitizer.Sanitization;

public class SanitizationAbort extends Abort {
    @Override
    public void abort(IASTaintAware taintAware, String sink, String category, List<StackTraceElement> stackTrace) {
        String taintedString = taintAware.toIASString().getString();
        List<IASTaintRange> ranges = taintAware.toIASString().getTaintRanges();
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\" of category \"%s\"! \n", taintAware, sink, category);
        // sanitize here
        String sanitizedString = "NOT IMPLEMENTED";//Sanitization.sanitizeSinks(taintedString, ranges, List.of(Sanitization.AttackCategory.XSS));
        System.err.println(taintedString + " was sanitized and resultet in: " + sanitizedString);
        taintAware.setContent(sanitizedString, ranges);
    }

    @Override
    public String getName() {
        return "sanitization";
    }
}
