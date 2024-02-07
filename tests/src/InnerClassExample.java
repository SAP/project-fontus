// From https://docs.oracle.com/javase/tutorial/java/javaOO/innerclasses.html
public class InnerClassExample {

    // Create an array
    private final static int SIZE = 15;
    private int[] arrayOfInts = new int[SIZE];

    public InnerClassExample() {
        // fill the array with ascending integer values
        for (int i = 0; i < SIZE; i++) {
            arrayOfInts[i] = i;
        }
    }

    public void printEven() {

        // Print out values of even indices of the array
        InnerClassExampleIterator iterator = this.new EvenIterator();
        while (iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        System.out.println();
    }

    interface InnerClassExampleIterator extends java.util.Iterator<Integer> { }

    // Inner class implements the InnerClassExampleIterator interface,
    // which extends the Iterator<Integer> interface

    private class EvenIterator implements InnerClassExampleIterator {

        // Start stepping through the array from the beginning
        private int nextIndex = 0;

        public boolean hasNext() {

            // Check if the current element is the last in the array
            return (nextIndex <= SIZE - 1);
        }

        public Integer next() {

            // Record a value of an even index of the array
            Integer retValue = Integer.valueOf(arrayOfInts[nextIndex]);

            // Get the next even element
            nextIndex += 2;
            return retValue;
        }
    }

    public static void main(String s[]) {

        // Fill the array with integer values and print out only
        // values of even indices
        InnerClassExample ds = new InnerClassExample();
        ds.printEven();
    }
}
