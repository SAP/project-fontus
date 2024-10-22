import java.util.Properties;

class MyProperties extends Properties {

    public synchronized Object put(Object var1, Object var2) {
        System.out.printf("Putting: %s -> %s\n", var1, var2);
        return null;
    }
}