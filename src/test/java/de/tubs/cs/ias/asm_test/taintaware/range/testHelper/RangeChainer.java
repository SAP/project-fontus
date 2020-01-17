package de.tubs.cs.ias.asm_test.taintaware.range.testHelper;

import de.tubs.cs.ias.asm_test.taintaware.range.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.range.IASTaintSource;

import java.util.ArrayList;
import java.util.List;

public class RangeChainer {
    private ArrayList<IASTaintRange> ranges = new ArrayList<>();

    public static RangeChainer range(int start, int end, IASTaintSource source) {
        return range(start, end, source.getId());
    }

    public static RangeChainer range(int start, int end, int source) {
        RangeChainer instance = new RangeChainer();
        instance.add(start, end, source);

        return instance;
    }

    public List<IASTaintRange> done() {
        return ranges;
    }


    public RangeChainer add(int start, int end, IASTaintSource source) {
        return add(start, end, source.getId());
    }


    public RangeChainer add(int start, int end, int source) {
        if (source < Short.MIN_VALUE || source > Short.MAX_VALUE) {
            throw new IndexOutOfBoundsException(Integer.toString(source));
        }

        ranges.add(new IASTaintRange(start, end, (short) source));

        return this;
    }
}
