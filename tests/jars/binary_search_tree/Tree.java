import java.util.Arrays;

/**
 * A binary tree
 */
public class Tree {

    private int value;
    private Tree lhs;
    private Tree rhs;

    /**
     * Creates a new tree with value as its root
     * @param value The initial value
     */
    Tree(int value) {
        this.value = value;
    }

    /**
     * Adds a new number into the tree
     * @param value The number to add
     */
    void insert(int value)  {
        if(this.value == value) {
            return;
        }

        if(value < this.value) {
            if(this.lhs == null) {
                this.lhs = new Tree(value);
            } else {
                this.lhs.insert(value);
            }
        }

        if(value > this.value) {
            if(this.rhs == null) {
                this.rhs = new Tree(value);
            } else {
                this.rhs.insert(value);
            }
        }
    }

    /**
     * A degenerate tree is a tree whose nodes all have zero or one children.
     * This method tests whether this tree is degenerate.
     * @return Is this tree degenerate?
     */
    boolean isDegenerate() {
        if(this.lhs != null && this.rhs != null) {
            return false;
        }

        if(this.lhs != null) {
            return this.lhs.isDegenerate();
        }

        if(this.rhs != null) {
            return this.rhs.isDegenerate();
        }

        // both null
        return false;
    }

    /**
     * Does the tree contain a value?
     * @param value The value to test
     * @return Is value in the tree?
     */
    boolean contains(int value) {
        if(this.value == value) {
            return true;
        }

        if(this.lhs != null && value < this.value) {
            return this.lhs.contains(value);
        }
        if(this.rhs != null && value > this.value) {
            return this.rhs.contains(value);
        }
        return false;
    }

    /**
     * Get all the tree's values in an array
     * @return All numbers in the tree
     */
    int[] values() {
        int[] values = new int[this.size()];
        int index = 0;
        int collected = this.collect(values, index);
        assert collected == this.size();
        return values;
    }

    /**
     * Collects the tree's values in an array
     * @param values The array to store the numbers in
     * @param index The index of the next number to insert
     * @return The index where the collecting shall continue
     */
    private int collect(int[] values, int index) {
        if(this.lhs != null) {
            index = this.lhs.collect(values, index);
        }
        values[index] = this.value;
        index++;
        if(this.rhs != null) {
            index = this.rhs.collect(values, index);
        }
        return index;
    }

    /**
     * The number of values in the tree
     * @return Number of nodes
     */
    private int size() {
        int lhsSize = this.lhs != null ? this.lhs.size() : 0;
        int rhsSize = this.rhs != null ? this.rhs.size() : 0;

        return 1 + lhsSize + rhsSize;
    }

    /**
     * Retrieves the largest element of the tree
     * @return The maximum number in the tree
     */
    int max() {
        if (this.rhs == null) {
            return this.value;
        }
        return this.rhs.max();
    }

    /**
     * The tree's height
     * @return The height..
     */
    int height() {
        int leftHeight = 1 + (this.lhs == null ? 0 : this.lhs.height());
        int rightHeight = 1 + (this.rhs == null ? 0 : this.rhs.height());
        return Math.max(leftHeight, rightHeight);
    }

    /**
     * Retrieves the smallest element of the tree
     * @return The minimum number in the tree
     */
    int min() {
        if (this.lhs == null) {
            return this.value;
        }
        return this.lhs.min();
    }

    /**
     * Formats the tree as an S-Expression
     * @return The formatted tree
     */
    private String toSEXP() {
        String lhsSexp = this.lhs == null ? "" : String.format("%s ", this.lhs.toSEXP());
        String rhsSexp = this.rhs == null ? "" : String.format(" %s", this.rhs.toSEXP());

        return String.format("(%s%d%s)", lhsSexp, this.value, rhsSexp);
    }

    /**
     * Tests whether a predicate is true for all numbers in the tree
     * @param function The predicate to test
     * @return Does the predicate hold for all contained numbers?
     */
    boolean forAll(Predicate function) {
        boolean result = true;
        if(this.lhs != null) {
            result = this.lhs.forAll(function);
        }
        result = result && function.apply(this.value);
        if(this.rhs != null) {
            result = result && this.rhs.forAll(function);
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        // See Effective Java (2nd Edition) page 40ff on how to effectively implement equals
        if(!(other instanceof Tree)) {
            return false;
        }
        Tree t = (Tree) other;
        return Arrays.equals(this.values(), t.values());
    }

    private static final int indent = 2;

    public String printTree(int increment) {
        String tree = "";
        String fill = "";

        for (int i = 0; i < increment; ++i) {
            fill = fill + " ";
        }

        String breakline = "";
        if (this.lhs != null) {
            tree += this.lhs.printTree(increment + indent);
            breakline = "\n";
        }
        tree += breakline + fill + this.value;

        if (this.rhs != null) {
            tree += breakline + this.rhs.printTree(increment + indent);
        }
        return tree;
    }

    @Override
    public String toString() {
        return this.toSEXP();
    }
}
