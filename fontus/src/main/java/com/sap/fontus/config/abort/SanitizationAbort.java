package com.sap.fontus.config.abort;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.*;

import com.sap.fontus.sanitizer.Sanitization;

public class SanitizationAbort extends Abort {

    private static final List<IASString> alreadySanitized = new ArrayList<>();

    @Override
    public IASTaintAware abort(IASTaintAware taintAware, Object instance, String sinkFunction, String sinkName, List<StackTraceElement> stackTrace) {
        IASString taintedIASString = taintAware.toIASString();
        String taintedString = taintedIASString.getString();
        IASTaintInformationable taintInfo = taintAware.getTaintInformation();
        System.err.print("List: ");
        alreadySanitized.forEach(s -> System.err.print(s.toString() + ", "));
        System.err.println();
        // until categories are specified by the taint mechanism, use:
        List<String> categories = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction).getCategories();
        System.err.printf("String \"%s\" is tainted and reached sink \"%s\": \"%s\" of categories %s! \n", taintAware, sinkName, sinkFunction, categories);
        // sanitize here
        for (IASString s : alreadySanitized) {
            if (s == taintedIASString) {
                System.err.println(taintedString + " was NOT sanitized since it was already sanitized before.");
                return taintAware;
            }
        }
        String sanitizedString = Sanitization.sanitizeSinks(taintedString, taintInfo, categories);
        alreadySanitized.add(taintedIASString);
        alreadySanitized.sort(Comparator.comparing(IASString::toString));
        System.err.println(taintedString + " was sanitized and resulted in: " + sanitizedString);

        taintAware = taintAware.newInstance();
        taintAware.setContent(sanitizedString, taintInfo);
        return taintAware;
    }

    @Override
    public String getName() {
        return "sanitization";
    }

    static {
        Abort.add(new SanitizationAbort());
    }
}
