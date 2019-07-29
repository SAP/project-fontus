package de.tubs.cs.ias.asm_test;

import java.util.Objects;

/**
 * Tuples..
 * Based on https://stackoverflow.com/a/12328838
 */
public final class Tuple<X, Y> {
    final X x;
    final Y y;

    private Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)", this.x, this.y);
    }

    static <T, U> Tuple<T, U> of(T x, U y) {
        return new Tuple<>(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(this.x, tuple.x) &&
                Objects.equals(this.y, tuple.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }
}