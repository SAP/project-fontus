import de.tubs.cs.ias.asm_test.config.Configuration;

public class Test {
    public static void main(String[] args) {
        System.out.println(Configuration.getConfiguration().getTaintMethod());
    }
}
