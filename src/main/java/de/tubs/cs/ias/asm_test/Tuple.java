package de.tubs.cs.ias.asm_test;

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
        return "(" + this.x + "," + this.y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Tuple)){
            return false;
        }

        Tuple<X,Y> other = (Tuple<X,Y>) obj;

        // this may cause NPE if nulls are valid values for x or y. The logic may be improved to handle nulls properly, if needed.
        return other.x.equals(this.x) && other.y.equals(this.y);
    }

    static <T,U> Tuple<T,U> of(T x, U y) {
        return new Tuple<>(x, y);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.x == null) ? 0 : this.x.hashCode());
        result = prime * result + ((this.y == null) ? 0 : this.y.hashCode());
        return result;
    }
}