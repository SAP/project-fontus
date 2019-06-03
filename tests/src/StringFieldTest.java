class StringFieldTest {

    private String str;

    public StringFieldTest(String s) {
        this.str = s;
    }

    public String getStr() {
        return this.str;
    }

    @Override
    public String toString() {
        return this.str;
    }

    public static void main(String[] args) {

        StringFieldTest sft1 = new StringFieldTest("hello");
        StringFieldTest sft2 = new StringFieldTest("world");
        System.out.println(sft1 + " " + sft2);
    }

}
