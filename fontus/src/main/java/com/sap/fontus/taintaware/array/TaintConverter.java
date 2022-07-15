package com.sap.fontus.taintaware.array;

import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class TaintConverter {
    private TaintConverter() {
    }

    static List<IASTaintRange> toTaintRanges(IASTaintMetadata[] taints) {
        List<IASTaintRange> ranges = new ArrayList<>();
        int start = 0;
        IASTaintMetadata taint = null;
        for (int i = 0; i < taints.length; i++) {
            if (taints[i] != taint) {
                if (taint != null && start != i) {
                    ranges.add(new IASTaintRange(start, i, taint));
                }
                start = i;
                taint = taints[i];
            }
        }
        if (taint != null) {
            ranges.add(new IASTaintRange(start, taints.length, taint));
        }
        return ranges;
    }

    static IASTaintMetadata[] toTaintArray(int size, List<IASTaintRange> ranges) {
        IASTaintMetadata[] taints = new IASTaintMetadata[size];
        if (ranges != null) {
            for (IASTaintRange range : ranges) {
                Arrays.fill(taints, range.getStart(), range.getEnd(), range.getMetadata());
            }
        }
        return taints;
    }
}
