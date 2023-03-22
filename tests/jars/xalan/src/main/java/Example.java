import org.apache.xml.utils.*;

public class Example {
    private XMLString xs = null;

    public Example() {
        xs = new XMLStringDefault("foo");
    }

    public String toString() {
        return xs.toString();
    }
    public static void main(String[] args) {
        Example e = new Example();
        System.out.printf("Made widget: '%s'%n", e.toString());
    }

}
