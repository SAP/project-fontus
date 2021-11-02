package com.sap.fontus.taintaware.range.testHelper;

import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
public class RangeChainer {
    private ArrayList<IASTaintRange> ranges = new ArrayList<>();

    public static RangeChainer range(int start, int end, IASTaintMetadata data) {
        RangeChainer instance = new RangeChainer();
        instance.add(start, end, data);
        return instance;
    }

    public static RangeChainer range(int start, int end, int source) {
        RangeChainer instance = new RangeChainer();
        instance.add(start, end, source);
        return instance;
    }

    public List<IASTaintRange> done() {
        return ranges;
    }


    public RangeChainer add(int start, int end, IASTaintMetadata data) {
        ranges.add(new IASTaintRange(start, end, data));
        return this;
    }

    public RangeChainer add(int start, int end, IASTaintSource source) {
        return add(start, end, new IASBasicMetadata(source));
    }

    public RangeChainer add(int start, int end, int source) {
        if (source < Short.MIN_VALUE || source > Short.MAX_VALUE) {
            throw new IndexOutOfBoundsException(Integer.toString(source));
        }

        ranges.add(new IASTaintRange(start, end, new IASBasicMetadata(source)));

        return this;
    }
}
