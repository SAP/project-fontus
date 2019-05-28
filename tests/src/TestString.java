class TestString {

    private String text;

    public TestString(String init) {
        this.text = init;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static void main(String[] args) {
        String input = "hello";
        TestString ts = new TestString(input);
        String str = ts.getText();
        ts.setText(input + str);
        System.out.println(ts.getText());
    }
}
