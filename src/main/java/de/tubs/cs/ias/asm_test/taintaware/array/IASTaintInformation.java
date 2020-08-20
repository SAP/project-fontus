package de.tubs.cs.ias.asm_test.taintaware.array;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintInformationable;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintRange;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASTaintSource;

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
    public void setTaint(int start, int end, IASTaintSource source) {
        setTaint(start, end, source.getId());
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

    @Override
    public List<IASTaintRange> getTaintRanges() {
        return TaintConverter.toTaintRanges(this.taints);
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

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public IASTaintInformation clone() {
        return new IASTaintInformation(this.taints.clone());
    }

    public int[] getTaints() {
        int[] dst = new int[this.taints.length];
        System.arraycopy(this.taints, 0, dst, 0, this.taints.length);
        return dst;
    }

    public void removeTaintFor(int start, int end, boolean leftShiftRangesAfterClearedArea) {
        this.checkBounds(start, end);
        if (leftShiftRangesAfterClearedArea) {
            int length = end - start;
            int remainder = this.taints.length - end;
            System.arraycopy(this.taints, end, this.taints, start, remainder);
            System.arraycopy(new int[length], 0, this.taints, start + remainder, length);
        } else {
            for (int i = start; i < end; i++) {
                this.taints[i] = 0;
            }
        }
    }

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

    public void resize(int size) {
        if (this.taints.length == size) {
            return;
        }
        int[] old = this.taints;
        this.taints = new int[size];
        int copyLength = Math.min(old.length, size);
        System.arraycopy(old, 0, this.taints, 0, copyLength);
    }

    public void removeAll() {
        this.taints = null;
    }

    public void replaceTaint(int start, int end, int[] taints) {
        this.removeTaintFor(start, end, true);
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
        }
    }

    public int getLength() {
        return this.taints.length;
    }

    public IASTaintSource getTaintFor(int position) {
        if (this.taints[position] == 0) {
            return null;
        }
        return IASTaintSource.getInstanceById((short) this.taints[position]);
    }

    public boolean isTaintedAt(int index) {
        return this.taints[index] != 0;
    }
}
