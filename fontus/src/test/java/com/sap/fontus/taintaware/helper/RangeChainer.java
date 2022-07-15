package com.sap.fontus.taintaware.helper;

import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintMetadata;
import com.sap.fontus.taintaware.shared.IASTaintRange;
import com.sap.fontus.taintaware.shared.IASTaintSource;

import java.util.ArrayList;
import java.util.List;

public class RangeChainer {
    private final List<IASTaintRange> ranges = new ArrayList<>();

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
        return this.ranges;
    }


    public RangeChainer add(int start, int end, IASTaintMetadata data) {
        this.ranges.add(new IASTaintRange(start, end, data));
        return this;
    }

    public RangeChainer add(int start, int end, IASTaintSource source) {
        return this.add(start, end, new IASBasicMetadata(source));
    }

    public RangeChainer add(int start, int end, int source) {
        if (source < Short.MIN_VALUE || source > Short.MAX_VALUE) {
            throw new IndexOutOfBoundsException(Integer.toString(source));
        }

        this.ranges.add(new IASTaintRange(start, end, new IASBasicMetadata(source)));

        return this;
    }
}
