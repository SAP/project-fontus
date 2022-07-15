package com.sap.fontus.taintaware.shared;

import java.util.ArrayList;
import java.util.List;

public final class IASTaintRangeUtils {
    private IASTaintRangeUtils() {
    }

    /**
     * Cuts the ranges at the beginning and the end so that it's within the specified bounds. Afterwords it will be shifted.
     *
     * @param startIndex inclusive
     * @param endIndex   exclusive (the same as for ranges)
     */
    public static void adjustRanges(List<IASTaintRange> ranges, int startIndex, int endIndex, int leftShift) {
        if (endIndex < startIndex || startIndex < 0) {
            throw new IllegalArgumentException("startIndex: " + startIndex + ", endIndex: " + endIndex);
        } else if (endIndex == startIndex) {
            ranges.clear();
            return;
        }

        if (ranges.isEmpty()) {
            return;
        }

        IASTaintRange first = ranges.get(0);

        // Special handling for ranges.size == 1, because its faster and it avoids problems of touching the same range twice when using the > 1 logic
        if (ranges.size() == 1) {
            ranges.set(0, new IASTaintRange(Math.max(first.getStart(), startIndex) - leftShift, Math.min(first.getEnd(), endIndex) - leftShift, first.getMetadata()));
            return;
        }

        ranges.set(0, new IASTaintRange(Math.max(first.getStart(), startIndex) - leftShift, first.getEnd() - leftShift, first.getMetadata()));

        IASTaintRange last = ranges.get(ranges.size() - 1);
        ranges.set(ranges.size() - 1, new IASTaintRange(last.getStart() - leftShift, Math.min(last.getEnd(), endIndex) - leftShift, last.getMetadata()));

        if (leftShift == 0) {
            return;
        }

        for (int i = 1; i < ranges.size() - 1; i++) {
            ranges.set(i, ranges.get(i).shiftRight(-leftShift));
        }
    }

    /**
     * Cuts the ranges at the beginning and the end so that it's within the specified bounds. Afterwords it will be shifted.
     * Ranges which are completely out of bounds will be cut out
     *
     * @param ranges     Ranges to adjust (operates on the list directly)
     * @param startIndex inclusive
     * @param endIndex   exclusive (the same as for ranges)
     */
    public static void adjustAndRemoveRanges(List<IASTaintRange> ranges, int startIndex, int endIndex, int leftShift) {
        if (endIndex < startIndex || startIndex < 0) {
            throw new IllegalArgumentException("startIndex: " + startIndex + ", endIndex: " + endIndex);
        } else if (endIndex == startIndex) {
            ranges.clear();
            return;
        }

        if (ranges.isEmpty()) {
            return;
        }

        for (int i = 0; i < ranges.size(); i++) {
            IASTaintRange tr = ranges.get(i);
            if (tr.getEnd() <= startIndex || endIndex < tr.getStart() || Math.max(tr.getStart(), startIndex) == Math.min(tr.getEnd(), endIndex)) {
                ranges.remove(i);
                i--;
            } else {
                ranges.set(i, new IASTaintRange(Math.max(tr.getStart(), startIndex) - leftShift, Math.min(tr.getEnd(), endIndex) - leftShift, tr.getMetadata()));
            }
        }
    }

    public static List<IASTaintRange> delete(List<IASTaintRange> ranges, int start, int end, boolean shift) {
        List<IASTaintRange> before = new ArrayList<>(ranges);
        List<IASTaintRange> after = new ArrayList<>(ranges);
        IASTaintRangeUtils.adjustAndRemoveRanges(before, 0, start, 0);
        IASTaintRangeUtils.adjustAndRemoveRanges(after, end, Integer.MAX_VALUE, shift ? end - start : 0);
        List<IASTaintRange> taintRanges = new ArrayList<>(before.size() + after.size());
        taintRanges.addAll(before);
        taintRanges.addAll(after);
        IASTaintRangeUtils.merge(taintRanges);

        return taintRanges;
    }

    public static List<IASTaintRange> insertWithShift(List<IASTaintRange> ranges, List<IASTaintRange> incomingTaint, int start, int end) {
        List<IASTaintRange> before = new ArrayList<>(ranges);
        List<IASTaintRange> after = new ArrayList<>(ranges);

        adjustAndRemoveRanges(before, 0, start, 0);
        adjustAndRemoveRanges(after, start, Integer.MAX_VALUE, start - end);
        adjustAndRemoveRanges(incomingTaint, 0, end - start, -start);

        List<IASTaintRange> result = new ArrayList<>(before.size() + incomingTaint.size() + after.size());
        result.addAll(before);
        result.addAll(incomingTaint);
        result.addAll(after);
        merge(result);

        return result;
    }

    /**
     * Ensures that only ranges within an on the bounds will remain.
     * Ranges on the bound will not be adjusted
     *
     * @param taintRanges Ranges to adjust (operates on the list directly)
     * @param start       inclusive
     * @param end         exclusive
     */
    public static void removeOutOfBounds(List<IASTaintRange> taintRanges, int start, int end) {
        for (int i = 0; i < taintRanges.size(); i++) {
            IASTaintRange tr = taintRanges.get(i);
            if (!(tr.getStart() < end && start <= tr.getEnd())) {
                taintRanges.remove(i);
                i--;
            }
        }
    }

    public static void merge(List<IASTaintRange> taintRanges) {
        for (int i = 0; i < taintRanges.size() - 1; i++) {
            IASTaintRange first = taintRanges.get(i);
            IASTaintRange second = taintRanges.get(i + 1);

            // Remove zero length ranges
            if (first.getStart() == first.getEnd()) {
                taintRanges.remove(i);
                i--;
                continue;
            }

            if (first.getMetadata() == second.getMetadata() && first.getEnd() == second.getStart()) {
                IASTaintRange merged = new IASTaintRange(first.getStart(), second.getEnd(), first.getMetadata());
                taintRanges.set(i, merged);
                taintRanges.remove(i + 1);
                i--;
            }
        }
    }

    public static void shiftRight(List<IASTaintRange> ranges, int shift) {
        if (shift == 0) {
            return;
        }

        for (int i = 0; i < ranges.size(); i++) {
            IASTaintRange range = ranges.get(i);
            ranges.set(i, range.shiftRight(shift));
        }
    }

    public static void shiftLeft(List<IASTaintRange> ranges, int shift) {
        shiftRight(ranges, -shift);
    }

    public static String taintRangesAsString(IASTaintRanges ranges) {
        StringBuilder sb = new StringBuilder();
        if (ranges == null) {
            sb.append("null");
        } else {
            List<IASTaintRange> rangeslist = ranges.getTaintRanges();
            sb.append("length: ");
            sb.append(ranges.getLength());
            sb.append(" ranges: { ");
            for (IASTaintRange range : rangeslist) {
                sb.append("[ ").append(range.getStart());
                sb.append(", ").append(range.getEnd());
                sb.append(" ), ");
            }
            sb.append("}");
        }
        return sb.toString();
    }
}
