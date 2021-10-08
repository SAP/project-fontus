package com.sap.fontus.taintaware.range.testHelper;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class THelper {
    public static boolean isUninitialized(IASTaintAware s) {
        return s.isUninitialized();
    }

    public static IASTaintInformationable get(IASTaintAware sB) {
        sB.initialize();
        return sB.getTaintInformation();
    }
}
