class PropertyTest {

    public static void main(String[] args) {
        String prop = System.getProperty("java.version", "v2");
        System.out.println(prop);
    }
}
