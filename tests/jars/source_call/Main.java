import java.util.Arrays;

public class Main {

    private static String test(String arg) {
        String bla = "foo " + arg + " foo";
        String name = Helper.getName(arg);
        String ret = Helper.getFoo(name, bla);
        return "xyz " + ret;

    }

    public static void main(String[] args) {
        for(String arg : args) {
            String foo = test(arg);
            System.out.println(foo);
        }

    }
}
