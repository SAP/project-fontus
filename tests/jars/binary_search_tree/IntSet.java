/**
 * Set of integers
 */
public class IntSet {

    private Tree values = null;

    /**
     * Constructs the empty set.
     */
    public IntSet() {

    }

    /**
     * Constructs a non empty set
     * @param numbers The numbers to be initially included in the set
     */
    public IntSet(int[] numbers) {
        if(numbers.length == 0) { return; }
        this.values = new Tree(numbers[0]);
        for(int i=1;i < numbers.length; i++) {
            this.values.insert(numbers[i]);
        }
    }


    /**
     * Adds a new value to the set
     * @param value The value to add
     */
    public void insert(int value) {
        if (this.values == null) {
            this.values = new Tree(value);
        } else {
            this.values.insert(value);
        }
    }

    /**
     * Membership test
     * @param value The value whose membership to test
     * @return Is value in the set?
     */
    public boolean contains(int value) {
        return this.values.contains(value);
    }

    /**
     * Get all numbers in the set
     * @return The numbers included in the set as an integer array
     */
    public int[] values() {
        return this.values.values();
    }

    /**
     * Build the union between this and a second set
     * @param other The second set
     * @return The union of both
     */
    public IntSet union(IntSet other) {
        if(other == null) {
            throw new IllegalArgumentException("Argument for union can't be null!");
        }
        IntSet union = new IntSet();
        for(int v : this.values()) {
            union.insert(v);
        }
        for(int v : other.values()) {
            union.insert(v);
        }
        return union;
    }


    /**
     * Build the intersection between this and a second set
     * @param other The second set
     * @return The intersection of both
     */
    public IntSet intersection(IntSet other) {
        if(other == null) {
            throw new IllegalArgumentException("Argument for intersection can't be null!");
        }
        IntSet intersection = new IntSet();
        for(int v : this.values()) {
            if(other.contains(v)) {
                intersection.insert(v);
            }
        }
        return intersection;
    }

    @Override
    public boolean equals(Object other) {
        // See Effective Java (2nd Edition) page 40ff on how to effectively implement equals
        if(!(other instanceof IntSet)) {
            return false;
        }
        IntSet is = (IntSet) other;
        if(this.values != null) {
            return this.values.equals(is.values);
        }
        return false;
    }

    @Override
    public String toString() {
        int[] numbers = this.values.values();
        String[] sNumbers = new String[numbers.length];
        for(int i=0; i < numbers.length; i++) {
            sNumbers[i] = Integer.toString(numbers[i]);
        }

        return String.format("{%s}", String.join(",", sNumbers));
    }

    public static void main(String args[]) {
        int[] as = {2,7,9,5};
        IntSet a = new IntSet(as);

        System.out.println("a = " + a);
        int[] bs = {9,1,2,3,7};
        IntSet b = new IntSet(bs);
        System.out.println("b = " + b);

        int[] cs = {0,1,4,3,7};
        IntSet c = new IntSet(cs);
        System.out.println("c = " + c);

        try {
            a.union(null);
        } catch (IllegalArgumentException ex) {
            System.out.println(String.format("Caught exception: %s", ex.getMessage()));
        }
        try {
            a.intersection(null);
        } catch (IllegalArgumentException ex) {
            System.out.println(String.format("Caught exception: %s", ex.getMessage()));
        }
        System.out.println("a union b:");
        System.out.println(a.union(b));

        System.out.println("a union b union c:");
        System.out.println(a.union(b).union(c));

        System.out.println("a intersection b:");
        System.out.println(a.intersection(b));

        System.out.println("a intersection b intersection c:");
        System.out.println(a.intersection(b).intersection(c));
    }
}
