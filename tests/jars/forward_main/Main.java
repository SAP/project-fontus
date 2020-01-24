import java.lang.reflect.Method;

class Main {

    private static final String HARNESS_CLASS = "Runner";
    private static final String HARNESS_METHOD = "run";
    public static void main(String[] args) throws Exception {
        ClassLoader loader = Main.class.getClassLoader();
        Class runnerClass = loader.loadClass(HARNESS_CLASS);
        Method run = runnerClass.getDeclaredMethod(HARNESS_METHOD, String[].class);
        run.invoke(null, new Object[] { args });
    }
}
