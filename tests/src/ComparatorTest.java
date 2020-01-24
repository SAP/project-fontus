import java.util.Arrays;

class ComparatorTest {

    public static void main(String[] args) {
        Arrays.sort(args, String.CASE_INSENSITIVE_ORDER);
        for(String s : args) {
            System.out.println(s);
        }
    }

}
