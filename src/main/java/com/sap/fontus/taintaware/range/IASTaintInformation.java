package com.sap.fontus.taintaware.range;

import com.sap.fontus.taintaware.shared.IASTaintInformationable;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintRanges;

import java.util.List;

public class IASTaintInformation extends IASTaintRanges implements IASTaintInformationable {
    public IASTaintInformation() {
    }

    public IASTaintInformation(List<IASTaintRange> ranges) {
        super(ranges);
    }
}
