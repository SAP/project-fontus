class StaticInitializerTest {

    private static final String text;

    static {
        text = "what's up?";
    }

    public String getText() {
        return text;
    }

    public StaticInitializerTest() { }

    public static void main(String[] args) {
        StaticInitializerTest sit = new StaticInitializerTest();
        String txt = sit.getText();
        System.out.println(txt);
    }

}
