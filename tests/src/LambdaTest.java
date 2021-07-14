import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.function.Function;

class LambdaTest {

    public static void main(String[] args) {
        ArrayList<String> strs = new ArrayList<String>();
        strs.add("sup");
        strs.add("son");
        strs.add("?");

        for(String s : strs) {
            System.out.println(s);
        }

        Collections.sort(strs, (s1, s2) -> Integer.compare(s2.length(), s1.length()));
        for(String s : strs) {
            System.out.println(s);
        }

        IllegalStateException exception = createObject(IllegalStateException::new, "Native Constructor Test");
        System.out.println(exception.getMessage());

        //Collections.sort(strs, Comparator.comparingInt(String::length));
        //for(String s : strs) {
        //    System.out.println(s);
        //}
    }

    public static <T> T createObject(Function<? super String, ? extends T> constructor, String message) {
        return constructor.apply(message);
    }

}
