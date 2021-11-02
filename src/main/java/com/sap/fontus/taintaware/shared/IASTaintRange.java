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
    /**
     * Taint metadata (contains the source information and optional other stuff)
     */
    private final IASTaintMetadata data;

    public IASTaintRange(int start, int end, IASTaintMetadata data) {
        if (end < start) {
            throw new IllegalArgumentException("TaintRange size cannot be smaller than 0");
        }
        if (data == null) {
            throw new NullPointerException("Source was null");
        }
        this.start = start;
        this.end = end;
        this.data = data;
    }

    public IASTaintRange shiftRight(int shift) {
        if (start + shift < 0) {
            throw new IllegalArgumentException("Illegal shift argument. Through shifting start index would be negative!");
        }
        return new IASTaintRange(start + shift, end + shift, data);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    protected Object clone() {
        return new IASTaintRange(start, end, data);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public IASTaintMetadata getMetadata() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IASTaintRange that = (IASTaintRange) o;

        if (start != that.start) return false;
        if (end != that.end) return false;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TRange{" +
                "b=" + start +
                ", e=" + end +
                ", data=" + data +
                '}';
    }
}
