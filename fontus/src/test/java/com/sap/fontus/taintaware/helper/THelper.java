package com.sap.fontus.taintaware.helper;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

public final class THelper {
    private THelper() {
    }

    public static boolean isUninitialized(IASTaintAware s) {
        return s.isUninitialized();
    }

    public static IASTaintInformationable get(IASTaintAware sB) {
        sB.initialize();
        return sB.getTaintInformation();
    }
}
