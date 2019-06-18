import java.io.File;
import java.util.Scanner;

class ArgumentTests {

    public static void main(String[] args) throws Exception {

        File f = new File("/etc/mtab");
        Scanner sc = new Scanner(f, "UTF-8");
        String s = sc.findWithinHorizon(".", 2);
        System.out.println(s);
        sc.close();
    }
}
