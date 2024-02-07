class TestStaticFinalString {

    public static final String text = "static final string";

    public TestStaticFinalString() {
    }

    public String getText() {
        return TestStaticFinalString.text;
    }

    public static void main(String[] args) {
        TestStaticFinalString ts = new TestStaticFinalString();
        String str = ts.getText();
        System.out.println(str);
    }
}
