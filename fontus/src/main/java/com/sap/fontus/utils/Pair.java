package com.sap.fontus.utils;

import java.util.Objects;

public class Pair<X, Y> {
    public final X x;
    public final Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.x, pair.x) && Objects.equals(this.y, pair.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}