package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRangeUtils.adjustRanges;

public class IASTaintRanges {
    private List<IASTaintRange> ranges;

    public IASTaintRanges() {
        this.ranges = new ArrayList<>(1);
    }

    public IASTaintRanges(List<IASTaintRange> ranges) {
        this.ranges = new ArrayList<>(ranges);
    }

    public synchronized IASTaintRanges addRange(int start, int end, short sourceId) {
        if (start == end) {
            // No need to process ranges with length 0
            return this;
        }

        final IASTaintRange newRange = new IASTaintRange(start, end, sourceId);

        final int containedOrAdjacentTo_index = getListIndexOfFirstContainingOrAdjacentRange(start);

        int rightNeighbour_index = containedOrAdjacentTo_index;

        // Is the start contained in another range?
        if (containedOrAdjacentTo_index >= 0) {
            final IASTaintRange containedOrAdjacentToOrig = this.ranges.get(containedOrAdjacentTo_index);
            final IASTaintRange containedOrAdjacentTo = new IASTaintRange(containedOrAdjacentToOrig.getStart(), start, containedOrAdjacentToOrig.getSource());
            final int containedOrAdjacentToEnd = this.ranges.get(containedOrAdjacentTo_index).getEnd();

            // Remove the original range in case it got shrinked down to a length of zero (start == containedOrAdjaventTo.end, this might be some instructions faster)
            if (containedOrAdjacentTo.getStart() == start) {
                ranges.remove(containedOrAdjacentTo_index);
                rightNeighbour_index--;
            } else {
                // replace the immutable range with its shrinked version
                this.ranges.set(containedOrAdjacentTo_index, containedOrAdjacentTo);
            }
            // is the end of the new range in the same, already existing, range? If so it is completely contained in it
            // and we have to split it - we can also skip the rest of the algorithm because there are no neighbours for sure
            if (end <= containedOrAdjacentToEnd) {
                final IASTaintRange secondHalf = new IASTaintRange(end, containedOrAdjacentToEnd, containedOrAdjacentTo.getSource());

                this.ranges.add(rightNeighbour_index + 1, newRange);

                // we do not add the second half range if its length is zero
                if (secondHalf.getStart() < secondHalf.getEnd()) {
                    ranges.add(rightNeighbour_index + 2, secondHalf);
                }

                return this;
            }

            // not completely contained, shift rightNeighbour_index to actual right neighbour
            rightNeighbour_index++;
        } else {
            // containedOrAdjacentTo_index contains a negative value; its inverse would be the index where to insert the new range
            // in order to keep the list sorted
            rightNeighbour_index = containedOrAdjacentTo_index * -1 - 1;
        }

        // Are there any (more) neighbours on the right?
        while (rightNeighbour_index < this.ranges.size()) {
            final IASTaintRange rightNeighbour = this.ranges.get(rightNeighbour_index);

            if (rightNeighbour.getEnd() <= end) {
                // right neighbour is completely covered by new range
                ranges.remove(rightNeighbour_index);
            } else if (rightNeighbour.getStart() < end) {
                // it is at least partially covered by the new range.
                // we have to terminate the loop because we do not need to go the right anymore
                this.ranges.set(rightNeighbour_index, new IASTaintRange(end, rightNeighbour.getEnd(), rightNeighbour.getSource()));
                break;
            } else {
                // right neighbour is not even partially covered, i.e. there must be a gap between the range we partially covered and this neighbour.
                // We will not touch the neighbour at all and will terminate the loop because we do not need to go the right anymore
                break;
            }
        }

        ranges.add(rightNeighbour_index, newRange);

        return this;
    }

    /**
     * This method "cuts out" the tainted ranges from start to end.
     * If start or end lies within a range, the range will be cut at this point and only the part within the interval removed.
     *
     * @param start            inclusive
     * @param end              exclusive
     * @param newRanges        the ranges do not have to be adopted to the new place, this is done by this method
     * @param replacementWidth "width" of the newly inserted ranges (determines shift for the ranges behind the insertion)
     */
    public void replaceTaintInformation(int start, int end, List<IASTaintRange> newRanges, int replacementWidth, boolean shiftNewRanges) {
        List<IASTaintRange> leftSide = this.getRanges(0, start);
        if (start > 0) {
            adjustRanges(leftSide, 0, start, 0);
        }

        int leftShift = (end - start) - replacementWidth;

        List<IASTaintRange> rightSide = this.getAllRangesStartingAt(end);
        adjustRanges(rightSide, end, Integer.MAX_VALUE, leftShift);

        if (shiftNewRanges) {
            IASTaintRangeUtils.shiftRight(newRanges, start);
        }

        this.ranges.clear();
        this.appendRanges(leftSide);
        this.appendRanges(newRanges);
        this.appendRanges(rightSide);
        this.mergeRanges();
    }

    private void mergeRanges() {
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

    private int getListIndexOfFirstContainingOrAdjacentRange(int index) {
        return Collections.binarySearch(this.ranges, null, (range, irrelevant) -> {
            if (range.getStart() <= index && range.getEnd() > index) {
                return 0;
            } else if (range.getStart() > index) {
                return 1;
            } else {
                return -1;
            }
        });
    }

    public boolean isTainted() {
        return !ranges.isEmpty();
    }

    public void removeAll() {
        this.ranges.clear();
    }

    public synchronized void appendRangesFrom(IASTaintRanges other) {
        this.appendRangesFrom(other, 0);
    }

    /**
     * By default no merging of ranges
     */
    public synchronized void appendRangesFrom(IASTaintRanges other, int rightShift) {
        appendRangesFrom(other, rightShift, false);
    }

    public synchronized void appendRangesFrom(IASTaintRanges other, int rightShift, boolean merge) {
        List<IASTaintRange> ranges = other.getAllRanges();
        if (rightShift != 0) {
            IASTaintRangeUtils.shiftRight(ranges, rightShift);
        }
        this.appendRanges(ranges, merge);
    }

    public synchronized void resize(int start, int end, int leftShift) {
        List<IASTaintRange> ranges = this.getRanges(start, end);
        IASTaintRangeUtils.adjustRanges(ranges, start, end, leftShift);
        this.removeAll();
        this.appendRanges(ranges);
    }

    /**
     * This appends the ranges to the current range set
     * If there are merging is wanted, neighbors of the inserted ranges will be checked if they have the same source, if yes they will be merges to one range.
     * The merging will also affect the appended ranges itself, if they are next to each other and have the same source
     */
    public synchronized void appendRanges(List<IASTaintRange> ranges, boolean merge) {
        if (!merge) {
            this.ranges.addAll(ranges);
            return;
        }
        this.ranges.sort(Comparator.comparingInt(IASTaintRange::getStart));

        for (IASTaintRange range : ranges) {
            // Merge with range after, if existent
            int afterIndex = Collections.binarySearch(this.ranges, range, (o1, o2) -> o2.getEnd() - o1.getStart());
            if (afterIndex >= 0) {
                IASTaintRange afterRange = this.ranges.get(afterIndex);
                if (afterRange.getSource() == range.getSource()) {
                    range = new IASTaintRange(afterRange.getStart(), range.getEnd(), range.getSource());
                    this.ranges.remove(afterIndex);
                }
            }

            // Merge with range before, if existent
            int beforeIndex = Collections.binarySearch(this.ranges, range, (o1, o2) -> o1.getEnd() - o2.getStart());
            if (beforeIndex >= 0) {
                IASTaintRange beforeRange = this.ranges.get(beforeIndex);
                if (beforeRange.getSource() == range.getSource()) {
                    range = new IASTaintRange(beforeRange.getStart(), range.getEnd(), range.getSource());
                    this.ranges.remove(beforeIndex);
                } else {
                    beforeIndex++;
                }
            } else {
                beforeIndex = -(beforeIndex + 1);
            }
            this.ranges.add(beforeIndex, range);
        }
    }

    /**
     * This appends the ranges to the current range set
     * The ranges will not be merged
     *
     * @param ranges
     */
    public synchronized void appendRanges(List<IASTaintRange> ranges) {
        this.appendRanges(ranges, false);
    }

    /**
     * Finds all ranges which at least lie partly within the specified interval
     *
     * @param startIndex including
     * @param endIndex   excluding
     */
    public synchronized List<IASTaintRange> getRanges(int startIndex, int endIndex) {
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

    public synchronized List<IASTaintRange> getAllRanges() {
        return new ArrayList<>(this.ranges);
    }

    public synchronized void removeTaintFor(int start, int end, boolean leftShiftRangesAfterClearedArea) {
        if (end <= start || start < 0) {
            throw new IllegalArgumentException("start: " + start + ", end: " + end);
        }

        if (ranges.isEmpty()) {
            return;
        }

        final List<IASTaintRange> r1 = getRanges(0, start);
        if (!r1.isEmpty()) {
            // if r1 is not empty we can be sure, that start > 0 (and by this conditional
            // we also avoid an unnecessary methods call
            adjustRanges(r1, 0, start, 0);
        }

        final List<IASTaintRange> r2 = getAllRangesStartingAt(end);
        if (!r2.isEmpty()) {
            int leftShift = 0;
            if (leftShiftRangesAfterClearedArea) {
                leftShift = end - start;
            }
            adjustRanges(r2, end, r2.get(r2.size() - 1).getEnd(), leftShift);
        }

        ranges.clear();
        ranges.addAll(r1);
        ranges.addAll(r2);
    }

    private synchronized List<IASTaintRange> getAllRangesStartingAt(int startIndex) {
        // Requesting all ranges starting from behind the last range should be valid, therefore we have to make sure that the end index
        // is >= startIndex, otherwise getRanges() will throw an Exception
        int endIndex = startIndex;
        if (!ranges.isEmpty()) {
            endIndex = Math.max(ranges.get(ranges.size() - 1).getEnd(), startIndex);
        }
        return getRanges(startIndex, endIndex);
    }

    public synchronized void insert(int index, List<IASTaintRange> insertions, int width) {
        List<IASTaintRange> startRanges = new ArrayList<>();
        if (index < 0) {
            throw new IllegalArgumentException("Index must be 0 or greater!");
        } else if (index > 0) {
            startRanges.addAll(this.getRanges(0, index));
            IASTaintRangeUtils.adjustRanges(startRanges, 0, index, 0);
        }

        IASTaintRangeUtils.shiftRight(insertions, index);

        List<IASTaintRange> endRanges = this.getAllRangesStartingAt(index);
        IASTaintRangeUtils.adjustRanges(endRanges, index, Integer.MAX_VALUE, -width);

        this.ranges.clear();
        this.ranges.addAll(startRanges);
        this.ranges.addAll(insertions);
        this.ranges.addAll(endRanges);
    }

    public synchronized void reversed(int length) {
        List<IASTaintRange> r = this.getAllRanges();
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
        return new IASTaintRanges(this.getAllRanges());
    }

    public IASTaintRange getRange(int listIndex) {
        return this.ranges.get(listIndex);
    }

    /**
     * Performs a getRanges and cuts of the endings
     *
     * @param end exclusive
     */
    public List<IASTaintRange> cutTaint(int start, int end) {
        List<IASTaintRange> ranges = getRanges(start, end);
        adjustRanges(ranges, start, end, 0);
        return ranges;
    }

    public IASTaintRange cutTaint(int index) {
        List<IASTaintRange> ranges = cutTaint(index, index + 1);
        if (ranges.size() == 0) {
            return null;
        }
        return ranges.get(0);
    }

    public IASTaintSource getTaintFor(int position) {
        List<IASTaintRange> range = getRanges(position, position + 1);
        if (range.isEmpty()) {
            return null;
        } else {
            return IASTaintSource.getInstanceById(range.get(0).getSource());
        }
    }
}
