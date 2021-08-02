package com.sap.fontus.taintaware.array;

import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.List;
import java.util.Objects;

public class IASTaintInformation implements IASTaintInformationable {
    private int[] taints;

    public IASTaintInformation(int length) {
        this.taints = new int[length];
    }

    public IASTaintInformation(int[] taints) {
        this.taints = Objects.requireNonNull(taints);
    }

    public IASTaintInformation(int size, List<IASTaintRange> ranges) {
        this.taints = TaintConverter.toTaintArray(size, ranges);
    }

    /**
     * Sets the taint for a specific range. If a taint already exists it will be overwritten.
     *
     * @param start Inclusive start index. 0 <= start < length
     * @param end   Exclusive end index. start < end <= length
     * @param taint Taint information to set
     */
    public void setTaint(int start, int end, int taint) {
        if (end > this.taints.length) {
            this.resize(end);
        }
        for (int i = start; i < end && i < this.taints.length && i >= 0; i++) {
            this.taints[i] = taint;
        }
    }

    /**
     * Sets the taint for a specific range. If a taint already exists it will be overwritten.
     *
     * @param start  Inclusive start index. 0 <= start < length
     * @param end    Exclusive end index. start < end <= length
     * @param source Taint information to set
     */
    public IASTaintInformationable setTaint(int start, int end, IASTaintSource source) {
        setTaint(start, end, source.getId());
        return this;
    }

    public void setTaint(int offset, int[] taints) {
        if (offset + taints.length > this.taints.length) {
            int size = offset + taints.length;
            this.resize(size);
        }

        System.arraycopy(taints, 0, this.taints, offset, taints.length);
    }

    public int[] getTaints(int start, int end) {
        this.checkBounds(start, end);
        int length = end - start;
        int[] dst = new int[length];
        System.arraycopy(this.taints, start, dst, 0, length);
        return dst;
    }

    public IASTaintRanges getTaintRanges() {
        return new IASTaintRanges(this.taints.length, TaintConverter.toTaintRanges(this.taints));
    }

    @Override
    public IASTaintRanges getTaintRanges(int length) {
        return this.getTaintRanges();
    }

    public boolean isTainted() {
        if (this.taints == null) {
            return false;
        }
        for (int taint : this.taints) {
            if (taint != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IASTaintInformationable deleteWithShift(int start, int end) {
        this.checkBounds(start, end);
        int removeLength = end - start;
        int[] changed = new int[this.taints.length - removeLength];
        System.arraycopy(this.taints, 0, changed, 0, start);
        System.arraycopy(this.taints, end, changed, start, this.taints.length - end);
        this.taints = changed;
        return this;
    }

    @Override
    public IASTaintInformationable clearTaint(int start, int end) {
        for (int i = start; i < end; i++) {
            this.taints[i] = 0;
        }
        return this;
    }

    @Override
    public IASTaintInformationable replaceTaint(int start, int end, IASTaintInformationable taintInformation) {
        IASTaintInformation ti = (IASTaintInformation) taintInformation;
        int replacedLength = this.taints.length - (end - start) + ti.getLength();
        int[] replaced = new int[replacedLength];
        System.arraycopy(this.taints, 0, replaced, 0, start);
        System.arraycopy(ti.getTaints(), 0, replaced, start, ti.getLength());
        System.arraycopy(this.taints, end, replaced, start + ti.getLength(), this.taints.length - end);
        this.taints = replaced;

        return this;
    }

    @Override
    public IASTaintInformationable insertWithShift(int offset, IASTaintInformationable taintInformation) {
        return this.replaceTaint(offset, offset, taintInformation);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public IASTaintInformation copy() {
        return new IASTaintInformation(this.taints.clone());
    }

    @Override
    public IASTaintSource getTaint(int index) {
        return IASTaintSourceRegistry.getInstance().get(this.taints[index]);
    }

    @Override
    public IASTaintInformationable slice(int start, int end) {
        int length = end - start;
        int[] sliced = new int[length];
        System.arraycopy(this.taints, start, sliced, 0, length);

        return new IASTaintInformation(sliced);
    }

    public int[] getTaints() {
        int[] dst = new int[this.taints.length];
        System.arraycopy(this.taints, 0, dst, 0, this.taints.length);
        return dst;
    }

    @Override
    public void reversed() {
        for (int i = 0; i < this.taints.length / 2; i++) {
            this.switchTaint(i, this.taints.length - i - 1);
        }
    }

    public void switchTaint(int first, int second) {
        this.checkBounds(first, second);
        int buffer = this.taints[first];
        this.taints[first] = this.taints[second];
        this.taints[second] = buffer;
    }

    @Override
    public IASTaintInformationable resize(int size) {
        if (this.taints.length == size) {
            return this;
        }
        int[] old = this.taints;
        this.taints = new int[size];
        int copyLength = Math.min(old.length, size);
        System.arraycopy(old, 0, this.taints, 0, copyLength);
        return this;
    }

    public void replaceTaint(int start, int end, int[] taints) {
        this.deleteWithShift(start, end);
        this.insertTaint(start, taints);
    }

    public void insertTaint(int start, int[] taints) {
        int newStart = start + taints.length;
        if (start < this.taints.length) {
            int[] buffer = new int[this.taints.length - start];
            System.arraycopy(this.taints, start, buffer, 0, this.taints.length - start);
            this.setTaint(newStart, buffer);
        }
        this.setTaint(start, taints);
    }

    private void checkBounds(int start, int end) {
        if (end > this.taints.length || start < 0) {
            throw new IndexOutOfBoundsException();
        } else if (end < start) {
            throw new IllegalArgumentException("Start index greater then end index");
        }
    }

    public int getLength() {
        return this.taints.length;
    }

    public IASTaintSource getTaintFor(int position) {
        if (this.taints[position] == 0) {
            return null;
        }
        return IASTaintSourceRegistry.getInstance().get(this.taints[position]);
    }

    public boolean isTaintedAt(int index) {
        return this.taints[index] != 0;
    }
}
