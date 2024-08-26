import java.io.FileInputStream;

class Main {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("./msgs.properties");
        MyProperties props = new MyProperties();
        props.load(fis);
    }
}

