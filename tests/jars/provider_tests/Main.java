import java.security.Provider;

class Main {

    public static void main(String[] args) throws Exception {
        ClassLoader loader = Main.class.getClassLoader();
        Class<?> clazz = loader.loadClass("TestProvider");
        Provider testing = (Provider) clazz.newInstance();
        testing.put("a", "b");
    }
}
