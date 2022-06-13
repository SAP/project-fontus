import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class Main {


    public Main() {
    }


    public static void main(String[] args) throws FileNotFoundException {

        FileInputStream in = new FileInputStream(args[0]);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        boolean res = reader.lines().map(l -> {
            System.out.println(l);
            return true;
        }).reduce(true, (a, b) -> a & b);
        System.out.printf("Result: %b%n", res);
    }

}
