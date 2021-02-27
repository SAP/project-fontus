package com.sap.fontus.taintaware.array;

import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaintConverter {
    public static List<IASTaintRange> toTaintRanges(int[] taints) {
        List<IASTaintRange> ranges = new ArrayList<>();
        int start = 0;
        int taint = 0;
        for (int i = 0; i < taints.length; i++) {
            if (taints[i] != taint) {
                if (taint != 0 && start != i) {
                    IASTaintSource source = IASTaintSourceRegistry.getInstance().get(taint);
                    ranges.add(new IASTaintRange(start, i, source));
                }
                start = i;
                taint = taints[i];
            }
        }
        if (taint != 0) {
            ranges.add(new IASTaintRange(start, taints.length, taint));
        }
        return ranges;
    }

    public static int[] toTaintArray(int size, List<IASTaintRange> ranges) {
        int[] taints = new int[size];
        for (IASTaintRange range : ranges) {
            Arrays.fill(taints, range.getStart(), range.getEnd(), range.getSource().getId());
        }
        return taints;
    }
}
