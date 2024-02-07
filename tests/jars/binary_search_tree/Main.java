import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        Tree t = new Tree(7);
        int[] numbers = {4,6,5,2,1,3,15,19,11,9,13,8};

        for(int number : numbers) {
            t.insert(number);
        }

        Tree t2 = new Tree(7);
        int[] numbers2 = {4,6,2,15,19,9,8};

        for(int number : numbers2) {
            t2.insert(number);
        }
        System.out.println(t2);


        System.out.println("t2 values: " + Arrays.toString(t2.values()));
        System.out.println(t);
        System.out.println(String.format("Tree is degenerate? %b", t.isDegenerate()));
        System.out.println(String.format("Min: %d, Max: %d, Height: %d", t.min(), t.max(), t.height()));

        boolean allSmallerTwenty = t.forAll(v -> v < 20);
        boolean allGreaterTwo = t.forAll(v -> v > 2);
        System.out.println(String.format("All < 20: %b, All > 2: %b", allSmallerTwenty, allGreaterTwo));
        System.out.println(t.printTree(0));
    }
}
