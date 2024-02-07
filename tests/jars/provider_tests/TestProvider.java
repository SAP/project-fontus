import java.security.Provider;

class TestProvider extends Provider {

    public TestProvider() {
        super("TestProvider", "1.0", "testing");
    }

}
