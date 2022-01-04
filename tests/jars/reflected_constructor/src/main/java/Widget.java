public class Widget {

    private String name;
    private static int counter = 0;

    public Widget() {
        this.name = String.format("%d", counter++);
    }

    public String toString() {
        return String.format("Widget: %s", this.name);
    }

}
