import java.util.*;

public class ScannerEchoTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> strings = new ArrayList<>();
        while (true) {
            String s = scanner.next();
            if ("exit".equals(s)) {
                break;
            }
            strings.add(s);
        }
        String out = String.join("\n", strings);
        System.out.println(out);
    }
}