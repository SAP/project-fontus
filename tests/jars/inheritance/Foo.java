import java.io.Serializable;

class Foo extends BaseTwo implements Printable, Serializable {
    Foo() {
    }

    public void print() {
        System.out.println("Foo");
    }
}

