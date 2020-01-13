package de.tubs.cs.ias.asm_test.taintaware.range;

import de.tubs.cs.ias.asm_test.taintaware.IASTaintRange;

import java.util.List;

public class IASTaintRangeUtils {
    /**
     * Cuts the ranges at the beginning and the end so that it's within the specified bounds. Afterwords it will be shifted.
     *
     * @param startIndex inclusive
     * @param endIndex   exclusive (the same as for ranges)
     */
    public static void adjustRanges(List<IASTaintRange> ranges, int startIndex, int endIndex, int leftShift) {
        if (endIndex <= startIndex || startIndex < 0) {
            throw new IllegalArgumentException("startIndex: $startIndex, endIndex: $endIndex");
        }

        if (ranges.isEmpty()) {
            return;
        }

        IASTaintRange first = ranges.get(0);

        // Special handling for ranges.size == 1, because its faster and it avoids problems of touching the same range twice when using the > 1 logic
        if (ranges.size() == 1) {
            ranges.set(0, new IASTaintRange(Math.max(first.getStart(), startIndex) - leftShift, Math.min(first.getEnd(), endIndex) - leftShift, first.getSource()));
            return;
        }

        ranges.set(0, new IASTaintRange(Math.max(first.getStart(), startIndex) - leftShift, first.getEnd() - leftShift, first.getSource()));

        IASTaintRange last = ranges.get(ranges.size() - 1);
        ranges.set(ranges.size() - 1, new IASTaintRange(last.getStart() - leftShift, Math.min(last.getEnd(), endIndex) - leftShift, last.getSource()));

        if (leftShift == 0) return;

        for (int i = 1; i < ranges.size() - 1; i++) {
            ranges.set(i, ranges.get(i).shiftRight(-leftShift));
        }
    }

    public static void shiftRight(List<IASTaintRange> ranges, int shift) {
        if (shift == 0) {
            return;
        }

        for(int i = 0; i < ranges.size(); i++) {
            IASTaintRange range = ranges.get(i);
            ranges.set(i, range.shiftRight(shift));
        }
    }

    public static void shiftLeft(List<IASTaintRange> ranges, int shift) {
        shiftRight(ranges, -shift);
    }
}
