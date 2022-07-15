package com.sap.fontus.taintaware.shared;

import java.io.Serializable;
import java.util.Objects;

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

    public IASTaintRange() {
        this.start = -1;
        this.end = -1;
        this.data = null;
    }

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
        if (this.start + shift < 0) {
            throw new IllegalArgumentException("Illegal shift argument. Through shifting start index would be negative!");
        }
        return new IASTaintRange(this.start + shift, this.end + shift, this.data);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    protected Object clone() {
        return new IASTaintRange(this.start, this.end, this.data);
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public IASTaintMetadata getMetadata() {
        return this.data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        IASTaintRange that = (IASTaintRange) o;

        if (this.start != that.start) {
            return false;
        }
        if (this.end != that.end) {
            return false;
        }
        return this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = this.start;
        result = 31 * result + this.end;
        result = 31 * result + this.data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TRange{" +
                "b=" + this.start +
                ", e=" + this.end +
                ", data=" + this.data +
                '}';
    }
}
