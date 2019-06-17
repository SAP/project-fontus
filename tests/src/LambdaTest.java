import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;

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

        //Collections.sort(strs, Comparator.comparingInt(String::length));
        //for(String s : strs) {
        //    System.out.println(s);
        //}
    }

}
