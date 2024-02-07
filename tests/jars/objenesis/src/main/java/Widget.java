public class Widget {
    private String name;
    private int price;

    @Override
    public String toString() {
        return "Widget{" +
                "name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    public Widget() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
