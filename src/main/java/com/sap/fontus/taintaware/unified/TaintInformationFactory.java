package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.shared.IASTaintInformationable;
import com.sap.fontus.taintaware.shared.IASTaintRange;

import java.util.List;

public class TaintInformationFactory {
    public static IASTaintInformationable createTaintInformation(int size) {
        switch (Configuration.getConfiguration().getTaintMethod()) {
            case ARRAY:
                return new com.sap.fontus.taintaware.array.IASTaintInformation(size);
            case RANGE:
                return new com.sap.fontus.taintaware.range.IASTaintInformation();
            case BOOLEAN:
                return com.sap.fontus.taintaware.bool.IASTaintInformation.UNTAINTED;
            case LAZYBASIC:
                return new com.sap.fontus.taintaware.lazybasic.IASTaintInformation();
            case LAZYCOMPLEX:
                return new com.sap.fontus.taintaware.lazycomplex.IASTaintInformation();
            case UNTAINTED:
                return com.sap.fontus.taintaware.untainted.IASTaintInformation.INSTANCE;
            default:
                throw new IllegalStateException("No taint method set. This should never happen!");
        }
    }

    public static IASTaintInformationable createTaintInformation(int size, List<IASTaintRange> ranges) {
        if (ranges == null || ranges.size() == 0) {
            return createTaintInformation(size);
        }

        switch (Configuration.getConfiguration().getTaintMethod()) {
            case ARRAY:
                return new com.sap.fontus.taintaware.array.IASTaintInformation(size, ranges);
            case RANGE:
                return new com.sap.fontus.taintaware.range.IASTaintInformation(ranges);
            case BOOLEAN:
                return com.sap.fontus.taintaware.bool.IASTaintInformation.TAINTED;
            case LAZYBASIC:
                return new com.sap.fontus.taintaware.lazybasic.IASTaintInformation(ranges);
            case LAZYCOMPLEX:
                return new com.sap.fontus.taintaware.lazycomplex.IASTaintInformation(ranges);
            case UNTAINTED:
                return com.sap.fontus.taintaware.untainted.IASTaintInformation.INSTANCE;
            default:
                throw new IllegalStateException("No taint method set. This should never happen!");
        }
    }
}
