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
        if (isUninitialized()) {
            return new int[end - start];
        }
        int length = end - start;
        int[] dst = new int[length];
        System.arraycopy(this.taints, start, dst, 0, length);
        return dst;
    }

    public void mergeTaint(int offset, int[] taints) {
        initialize();
        if (offset + taints.length > this.length) {
            throw new IndexOutOfBoundsException();
        }

        for (int i = offset; i < offset + taints.length; i++) {
            this.taints[i] |= taints[i];
        }
    }

    /**
     * Merges the taint for a specific range by a bitwise OR-Operation. If a taint does not exist, it will be set to the passed taint
     *
     * @param start Inclusive start index. 0 <= start < length
     * @param end   Exclusive end index. start < end <= length
     * @param taint Taint information to set
     */
    public void mergeTaint(int start, int end, int taint) {
        initialize();
        for (int i = start; i < end && i < this.length && i >= 0; i++) {
            this.taints[i] |= taint;
        }
    }

    public void mergeTaint(IASTaintInformation information) {
        initialize();
        if (this.length != information.length) {
            throw new IllegalArgumentException("Different taint information lengths are not mergable");
        }
        for (int i = 0; i < this.length; i++) {
            this.taints[i] |= information.taints[i];
        }
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
        if (isUninitialized()) {
            return;
        }
        if (leftShiftRangesAfterClearedArea) {
            int length = end - start;
            for (int i = this.length; i <= start; i--) {
                int old = 0;
                if (i + length < this.length) {
                    old = this.taints[i + length];
                }
                this.taints[i] = old;
            }
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
        if (isUninitialized()) {
            return;
        }
        int buffer = this.taints[first];
        this.taints[first] = this.taints[second];
        this.taints[second] = buffer;
    }

    public void resize(int size) {
        this.length = size;
        if (isUninitialized()) {
            initialize();
        } else {
            int[] old = this.taints;
            this.taints = new int[size];
            System.arraycopy(old, 0, this.taints, 0, old.length);
        }
    }

    public void removeAll() {
        this.taints = new int[this.length];
    }

    public void insertTaint(int start, int[] taints) {
        initialize();
        int newStart = start + taints.length;
        int[] buffer = new int[this.length - start];
        System.arraycopy(this.taints, start, buffer, 0, this.taints.length - start);
        this.setTaint(newStart, buffer);
        this.setTaint(start, taints);
    }
}
