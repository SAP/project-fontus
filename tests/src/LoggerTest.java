import de.tubs.cs.ias.asm_test.utils.Logger;

class LoggerTest {

    static class RootLogger extends Logger {
        public RootLogger() {
            super("", null);
        }
    }

    public static void main(String[] args) {
        LoggerTest.RootLogger rl = new LoggerTest.RootLogger();
        // TODO: still fails, as we are trying to rewrite an inherited method
        //rl.info("sup");
    }
}
