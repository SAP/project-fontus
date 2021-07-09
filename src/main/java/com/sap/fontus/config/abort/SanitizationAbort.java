package com.sap.fontus.config.abort;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.IASStringable;
import com.sap.fontus.taintaware.shared.IASTaintRange;

import java.util.*;

import com.sap.fontus.sanitizer.Sanitization;

public class SanitizationAbort extends Abort {

    private static List<IASStringable> alreadySanitized = new ArrayList<>();

    @Override
    public void abort(IASTaintAware taintAware, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        IASStringable taintedIASString = taintAware.toIASString();
        String taintedString = taintedIASString.getString();
        List<IASTaintRange> ranges = taintAware.toIASString().getTaintRanges();
        System.err.print("List: ");
        alreadySanitized.forEach(s -> System.err.print(s.toString() + ", "));
        System.err.println();
        // until categories are specified by the taint mechanism, use:
        List<String> categories = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction).getCategories();
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\" of categories %s! \n", taintAware, sinkFunction, categories);
        // sanitize here
        for (IASStringable s : alreadySanitized) {
            if (s == taintedIASString) {
                System.err.println(taintedString + " was NOT sanitized since it was already sanitized before.");
                return;
            }
        }
        String sanitizedString = Sanitization.sanitizeSinks(taintedString, ranges, categories);
        alreadySanitized.add(taintedIASString);
        alreadySanitized.sort(Comparator.comparing(IASStringable::toString));
        System.err.println(taintedString + " was sanitized and resulted in: " + sanitizedString);
        taintAware.setContent(sanitizedString, ranges);
    }

    @Override
    public String getName() {
        return "sanitization";
    }
}
