package de.tubs.cs.ias.asm_test.taintaware.array;

public class IASTaintInformation {
    private final int length;
    private int[] taints;

    public IASTaintInformation(int length) {
        this.length = length;
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
        for (int i = start; i < end && i < this.length && i >= 0; i++) {
            this.taints[i] = taint;
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
        if (this.taints == null) {
            this.taints = new int[this.length];
        }
    }
}
