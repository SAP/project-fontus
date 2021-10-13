package com.sap.fontus.taintaware.shared;

import java.util.*;

public class IASTaintRanges implements Iterable<IASTaintRange> {
    private int length;
    protected List<IASTaintRange> ranges;

    public IASTaintRanges(int length) {
        this.length = length;
        this.ranges = new ArrayList<>(1);
    }

    public IASTaintRanges(int length, List<IASTaintRange> ranges) {
        this.length = length;
        this.ranges = new ArrayList<>(ranges);
    }

    public IASTaintRanges(int length, int source) {
        this.length = length;
        this.ranges = new ArrayList<>();
        this.ranges.add(new IASTaintRange(0, length, source));
    }

    public IASTaintRanges(int length, IASTaintSource taintSource) {
        this(length, taintSource.getId());
    }

    @Override
    public Iterator<IASTaintRange> iterator() {
        return ranges.iterator();
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public void sort(Comparator<IASTaintRange> comparator) {
        ranges.sort(comparator);
    }

    public synchronized void setTaint(int start, int end, IASTaintSource source) {
        this.setTaint(start, end, source != null ? source.getId() : 0);
        this.merge();
    }

    public synchronized void setTaint(int start, int end, int sourceId) {
        if (start == end) {
            // No need to process ranges with length 0
            return;
        }
        if (this.length < end) {
            end = this.length;
        }

        int insertionLength = end - start;

        IASTaintRanges begin = this.slice(0, start);
        IASTaintRanges after = this.slice(end, this.length);
        IASTaintRanges insertion = new IASTaintRanges(insertionLength, sourceId);

        this.ranges = begin.concat(insertion).concat(after).getTaintRanges();
        this.merge();
    }

    public void delete(int start, int end, boolean shift) {
        IASTaintRanges before = this.slice(0, start);
        IASTaintRanges after = this.slice(end, this.length);

        if (shift) {
            this.ranges = before.concat(after).getTaintRanges();
            this.length = this.length - (end - start);
        } else {
            IASTaintRanges empty = new IASTaintRanges(end - start);
            this.ranges = before.concat(empty).concat(after).getTaintRanges();
        }

        this.merge();
    }

    public IASTaintRanges slice(int start, int end) {
        List<IASTaintRange> ranges = new ArrayList<>(this.ranges);
        IASTaintRangeUtils.adjustAndRemoveRanges(ranges, start, end, start);
        return new IASTaintRanges(end - start, ranges);
    }

    public void merge() {
        this.ranges.sort(Comparator.comparingInt(IASTaintRange::getStart));

        for (int i = 0; i < this.ranges.size() - 1; i++) {
            IASTaintRange currRange = this.ranges.get(i);
            IASTaintRange nextRange = this.ranges.get(i + 1);

            // Is merge possible?
            if (currRange.getEnd() == nextRange.getStart() && currRange.getSource() == nextRange.getSource()) {
                IASTaintRange newRange = new IASTaintRange(currRange.getStart(), nextRange.getEnd(), nextRange.getSource());
                this.ranges.set(i, newRange);
                this.ranges.remove(i + 1);
                i--;
            }
        }
    }

    public boolean isTainted() {
        return !ranges.isEmpty();
    }

    public synchronized void resize(int length) {
        IASTaintRangeUtils.adjustAndRemoveRanges(this.ranges, 0, length, 0);
        this.length = length;
    }

    /**
     * Finds all ranges which at least lie partly within the specified interval
     *
     * @param startIndex including
     * @param endIndex   excluding
     */
    public synchronized List<IASTaintRange> getTaintRanges(int startIndex, int endIndex) {
        if (endIndex < startIndex || startIndex < 0) {
            throw new IndexOutOfBoundsException("startIndex: " + startIndex + ", endIndex: " + endIndex);
        } else if (endIndex == startIndex) {
            return new ArrayList<>(0);
        }

        List<IASTaintRange> affectedRanges = new ArrayList<>(this.ranges.size());

        for (IASTaintRange range : this.ranges) {
            if (range.getEnd() <= startIndex || endIndex <= range.getStart()) {
                // Outside range
                continue;
            }

            affectedRanges.add(range);
        }
        return affectedRanges;
    }

    public synchronized List<IASTaintRange> getTaintRanges() {
        return new ArrayList<>(this.ranges);
    }

    public IASTaintRanges concat(IASTaintRanges append) {
        IASTaintRanges copy = this.copy();

        List<IASTaintRange> appendRanges = append.getTaintRanges();
        IASTaintRangeUtils.shiftRight(appendRanges, copy.length);
        copy.ranges.addAll(appendRanges);
        copy.length = this.length + append.length;
        copy.merge();

        return copy;
    }

    /**
     * Inserts the given ranges at the passed position.
     * This shifts the inserted ranges by the index and also moves the ranges after the index by the length of the insertion.
     * @param index Position where the insertion is inserted
     * @param insertions ranges to insert
     */
    public synchronized void insertTaint(int index, IASTaintRanges insertions) {
        if (index > this.length) {
            // The ranges are inserted at the end of the existing string
            IASTaintRanges copy = this.copy();

            List<IASTaintRange> ranges = insertions.getTaintRanges();
            IASTaintRangeUtils.shiftRight(ranges, index);
            copy.ranges.addAll(ranges);
            copy.length = this.length + insertions.length;
            copy.merge();

            this.ranges = copy.getTaintRanges();
            this.length = copy.length;
        } else {
            // The ranges are inserted somewhere into the existing string
            IASTaintRanges start = this.slice(0, index);
            IASTaintRanges end = this.slice(index, this.length);
            IASTaintRanges result = start.concat(insertions).concat(end);
            result.merge();

            this.ranges = result.getTaintRanges();
            this.length = result.length;
        }
    }

    public synchronized void reversed() {
        List<IASTaintRange> r = this.getTaintRanges();
        List<IASTaintRange> newRanges = new ArrayList<IASTaintRange>(r.size());

        for (IASTaintRange range : r) {
            int newEnd = length - range.getStart();
            int newStart = newEnd - (range.getEnd() - range.getStart());
            IASTaintRange newRange = new IASTaintRange(newStart, newEnd, range.getSource());
            newRanges.add(0, newRange);
        }
        this.ranges.clear();
        this.ranges.addAll(newRanges);
    }

    public synchronized IASTaintRanges copy() {
        return new IASTaintRanges(this.length, this.getTaintRanges());
    }

    public boolean isTaintedAt(int index) {
        return getTaintFor(index) != null;
    }

    public IASTaintSource getTaintFor(int position) {
        for (IASTaintRange range : this.ranges) {
            if (range.getStart() <= position && position < range.getEnd()) {
                return range.getSource();
            }
        }
        return null;
    }

    public int getLength() {
        return length;
    }
}
