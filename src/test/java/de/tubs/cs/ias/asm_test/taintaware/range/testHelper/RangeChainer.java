package de.tubs.cs.ias.asm_test.taintaware.range.testHelper;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("ALL")
// David: As I didn't write this Code and don't want to mess with it I suppressed the warnings.
// TODO: Work out whether we can adapt it to the style of the remaining project?
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

        ranges.add(new IASTaintRange(start, end, source));

        return this;
    }
}
