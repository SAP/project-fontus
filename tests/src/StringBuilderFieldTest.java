class StringBuilderFieldTest {

    private StringBuilder sb;

    public StringBuilderFieldTest() {
        this.sb = new StringBuilder();
    }

    public String getStr() {
        return this.sb.toString();
    }
    public void append(String s) {
        this.sb.append(s);
    }

    @Override
    public String toString() {
        return this.getStr();
    }

    public static void main(String[] args) {

        StringBuilderFieldTest sbft = new StringBuilderFieldTest();
        sbft.append("hello");
        sbft.append(" ");
        sbft.append("world");
        sbft.append("!");
        System.out.println(sbft);
    }

}

