package de.tubs.cs.ias.asm_test.taintaware;

public class IASTaintRange implements Cloneable {
    /**
     * Inclusive the start index
     */
    private final int start;
    /**
     * Exclusive the end index
     */
    private final int end;
    private final short source;

    public IASTaintRange(int start, int end, short source) {
        if(end < start) {
            throw new IllegalArgumentException("TaintRange size cannot be smaller than 0");
        }
        this.start = start;
        this.end = end;
        this.source = source;
    }

    public IASTaintRange shiftRight(int shift) {
        if(start + shift < 0) {
            throw new IllegalArgumentException("Illegal shift argument. Through shifting start index would be negative!");
        }
        return new IASTaintRange(start + shift, end + shift, source);
    }

    @Override
    protected Object clone() {
        return new IASTaintRange(start, end, source);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public short getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IASTaintRange range = (IASTaintRange) o;

        if (start != range.start) return false;
        if (end != range.end) return false;
        return source == range.source;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + (int) source;
        return result;
    }
}
