package com.sap.fontus.taintaware.array;

import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;

import java.util.List;
import java.util.Objects;

public class IASTaintInformation implements IASTaintInformationable {
    private IASTaintMetadata[] taints;

    public IASTaintInformation(int length) {
        this.taints = new IASTaintMetadata[length];
    }

    public IASTaintInformation(IASTaintMetadata[] taints) {
        this.taints = Objects.requireNonNull(taints);
    }

    public IASTaintInformation(int size, List<IASTaintRange> ranges) {
        this.taints = TaintConverter.toTaintArray(size, ranges);
    }

    /**
     * Sets the taint for a specific range. If a taint already exists it will be overwritten.
     *
     * @param start  Inclusive start index. 0 <= start < length
     * @param end    Exclusive end index. start < end <= length
     * @param data Taint information to set
     */
    public IASTaintInformationable setTaint(int start, int end, IASTaintMetadata data) {
        if (start < 0) {
            start = 0;
        }
        if (end > this.taints.length) {
            this.resize(end);
        }
        for (int i = start; i < end; i++) {
            this.taints[i] = data;
        }
        return this;
    }

    public void setTaint(int offset, IASTaintMetadata[] taints) {
        if (offset + taints.length > this.taints.length) {
            int size = offset + taints.length;
            this.resize(size);
        }

        System.arraycopy(taints, 0, this.taints, offset, taints.length);
    }

    public IASTaintMetadata[] getTaints() {
        return getTaints(0, this.taints.length);
    }

    public IASTaintMetadata[] getTaints(int start, int end) {
        this.checkBounds(start, end);
        int length = end - start;
        IASTaintMetadata[] dst = new IASTaintMetadata[length];
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
        for (IASTaintMetadata taint : this.taints) {
            if (taint != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IASTaintInformationable deleteWithShift(int start, int end) {
        this.checkBounds(start, end);
        int removeLength = end - start;
        IASTaintMetadata[] changed = new IASTaintMetadata[this.taints.length - removeLength];
        System.arraycopy(this.taints, 0, changed, 0, start);
        System.arraycopy(this.taints, end, changed, start, this.taints.length - end);
        this.taints = changed;
        return this;
    }

    @Override
    public IASTaintInformationable clearTaint(int start, int end) {
        for (int i = start; i < end; i++) {
            this.taints[i] = null;
        }
        return this;
    }

    @Override
    public IASTaintInformationable replaceTaint(int start, int end, IASTaintInformationable taintInformation) {
        IASTaintInformation ti = (IASTaintInformation) taintInformation;
        int replacedLength = this.taints.length - (end - start) + ti.getLength();
        IASTaintMetadata[] replaced = new IASTaintMetadata[replacedLength];
        System.arraycopy(this.taints, 0, replaced, 0, start);
        System.arraycopy(ti.getTaints(), 0, replaced, start, ti.getLength());
        System.arraycopy(this.taints, end, replaced, start + ti.getLength(), this.taints.length - end);
        this.taints = replaced;

        return this;
    }

    @Override
    public IASTaintInformationable shiftRight(int offset) {
        int newSize = this.taints.length + offset;
        IASTaintMetadata newArray[] = new IASTaintMetadata[newSize];
        System.arraycopy(this.taints, 0, newArray, offset, this.taints.length);
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
    public IASTaintMetadata getTaint(int index) {
        return this.taints[index];
    }

    @Override
    public IASTaintInformationable slice(int start, int end) {
        int length = end - start;
        IASTaintMetadata[] sliced = new IASTaintMetadata[length];
        System.arraycopy(this.taints, start, sliced, 0, length);

        return new IASTaintInformation(sliced);
    }

    @Override
    public IASTaintInformationable reversed() {
        for (int i = 0; i < this.taints.length / 2; i++) {
            this.switchTaint(i, this.taints.length - i - 1);
        }
        return this;
    }

    public void switchTaint(int first, int second) {
        this.checkBounds(first, second);
        IASTaintMetadata buffer = this.taints[first];
        this.taints[first] = this.taints[second];
        this.taints[second] = buffer;
    }

    @Override
    public IASTaintInformationable resize(int size) {
        if (this.taints.length == size) {
            return this;
        }
        IASTaintMetadata[] old = this.taints;
        this.taints = new IASTaintMetadata[size];
        int copyLength = Math.min(old.length, size);
        System.arraycopy(old, 0, this.taints, 0, copyLength);
        return this;
    }

    public void replaceTaint(int start, int end, IASTaintMetadata[] taints) {
        this.deleteWithShift(start, end);
        this.insertTaint(start, taints);
    }

    public void insertTaint(int start, IASTaintMetadata[] taints) {
        int newStart = start + taints.length;
        if (start < this.taints.length) {
            IASTaintMetadata[] buffer = new IASTaintMetadata[this.taints.length - start];
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

    @Override
    public int getLength() {
        return this.taints.length;
    }

    public IASTaintMetadata getTaintFor(int position) {
        if (this.taints[position] == null) {
            return null;
        }
        return this.taints[position];
    }

    public boolean isTaintedAt(int index) {
        return this.taints[index] != null;
    }
}
