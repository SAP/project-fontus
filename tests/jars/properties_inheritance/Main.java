import java.io.FileInputStream;

class Main {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream(args[0]);
        MyProperties props = new MyProperties();
        props.load(fis);
    }
}

