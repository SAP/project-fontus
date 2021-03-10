package com.sap.fontus.taintaware.shared;

import java.io.Serializable;

public class IASTaintRange implements Cloneable, Serializable {
    /**
     * Inclusive the start index
     */
    private final int start;
    /**
     * Exclusive the end index
     */
    private final int end;
    private final IASTaintSource source;

    public IASTaintRange(int start, int end, IASTaintSource source) {
        if (end < start) {
            throw new IllegalArgumentException("TaintRange size cannot be smaller than 0");
        }
        if (source == null) {
            throw new NullPointerException("Source was null");
        }
        this.start = start;
        this.end = end;
        this.source = source;
    }

    public IASTaintRange(int start, int end, int sourceId) {
        this(start, end, IASTaintSourceRegistry.getInstance().get(sourceId));
    }

    public IASTaintRange shiftRight(int shift) {
        if (start + shift < 0) {
            throw new IllegalArgumentException("Illegal shift argument. Through shifting start index would be negative!");
        }
        return new IASTaintRange(start + shift, end + shift, source);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
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

    public IASTaintSource getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IASTaintRange that = (IASTaintRange) o;

        if (start != that.start) return false;
        if (end != that.end) return false;
        return source.equals(that.source);
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + source.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TRange{" +
                "b=" + start +
                ", e=" + end +
                ", src=" + source +
                '}';
    }
}
