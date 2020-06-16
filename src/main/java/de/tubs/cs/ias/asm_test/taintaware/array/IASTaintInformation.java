package de.tubs.cs.ias.asm_test.taintaware.array;

public class IASTaintInformation {
    private int length;
    private int[] taints;

    public IASTaintInformation(int length) {
        this.length = length;
    }

    public IASTaintInformation(int[] taints) {
        this.taints = taints;
        this.length = taints.length;
    }

    public boolean isInitialized() {
        return !isUninitialized();
    }

    public boolean isUninitialized() {
        return this.taints == null;
    }

    /**
     * Sets the taint for a specific range. If a taint already exists it will be overwritten.
     *
     * @param start Inclusive start index. 0 <= start < length
     * @param end   Exclusive end index. start < end <= length
     * @param taint Taint information to set
     */
    public void setTaint(int start, int end, int taint) {
        initialize();
        if (end > this.length) {
            this.resize(end);
        }
        for (int i = start; i < end && i < this.length && i >= 0; i++) {
            this.taints[i] = taint;
        }
    }

    public void setTaint(int offset, int[] taints) {
        initialize();
        if (offset + taints.length > this.length) {
            int size = offset + taints.length;
            this.resize(size);
        }

        System.arraycopy(taints, 0, this.taints, offset, taints.length);
    }

    public int[] getTaints(int start, int end) {
        this.checkBounds(start, end);
        if (isUninitialized()) {
            return new int[end - start];
        }
        int length = end - start;
        int[] dst = new int[length];
        System.arraycopy(this.taints, start, dst, 0, length);
        return dst;
    }

    public boolean isTainted() {
        if (this.taints == null) {
            return false;
        }
        for (int i = 0; i < this.length; i++) {
            if (this.taints[i] != 0) {
                return true;
            }
        }
        return false;
    }

    private void initialize() {
        if (isUninitialized()) {
            this.taints = new int[this.length];
        }
    }

    public IASTaintInformation clone() {
        if (isUninitialized()) {
            return new IASTaintInformation(this.length);
        }
        return new IASTaintInformation(this.taints.clone());
    }

    public int[] getTaints() {
        int[] dst = new int[this.length];
        if (isInitialized()) {
            System.arraycopy(this.taints, 0, dst, 0, this.length);
        }
        return dst;
    }

    public void removeTaintFor(int start, int end, boolean leftShiftRangesAfterClearedArea) {
        this.checkBounds(start, end);
        if (isUninitialized()) {
            return;
        }
        if (leftShiftRangesAfterClearedArea) {
            int length = end - start;
            int remainder = this.length - end;
            System.arraycopy(this.taints, end, this.taints, start, remainder);
            System.arraycopy(new int[length], 0, this.taints, start + remainder, length);
        } else {
            for (int i = start; i < end; i++) {
                this.taints[i] = 0;
            }
        }
    }

    public void reversed() {
        if (isUninitialized()) return;
        for (int i = 0; i < this.length / 2; i++) {
            this.switchTaint(i, this.length - i - 1);
        }
    }

    public void switchTaint(int first, int second) {
        this.checkBounds(first, second);
        if (isUninitialized()) {
            return;
        }
        int buffer = this.taints[first];
        this.taints[first] = this.taints[second];
        this.taints[second] = buffer;
    }

    public void resize(int size) {
        if (this.length == size) {
            return;
        }
        this.length = size;
        if (isInitialized()) {
            int[] old = this.taints;
            this.taints = new int[size];
            int copyLength = Math.min(old.length, size);
            System.arraycopy(old, 0, this.taints, 0, copyLength);
        }
    }

    public void removeAll() {
        this.taints = null;
    }

    public void replaceTaint(int start, int end, int[] taints) {
        this.removeTaintFor(start, end, true);
        this.insertTaint(start, taints);
    }

    public void insertTaint(int start, int[] taints) {
        initialize();
        int newStart = start + taints.length;
        if (start < this.length) {
            int[] buffer = new int[this.length - start];
            System.arraycopy(this.taints, start, buffer, 0, this.taints.length - start);
            this.setTaint(newStart, buffer);
        }
        this.setTaint(start, taints);
    }

    private void checkBounds(int start, int end) {
        if (end > length || start < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    public int getLength() {
        return this.length;
    }
}
