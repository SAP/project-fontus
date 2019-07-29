class StringBuilderReturnTest {

    private StringBuilder buf;

    public  StringBuilderReturnTest() {
        this.buf = new StringBuilder(16);
    }

    public void append(String str) {
        this.buf.append(str);
    }

    public StringBuilder getBuilder() {
        return this.buf;
    }

    public static void main(String[] args) {
        StringBuilderReturnTest sbrt = new StringBuilderReturnTest();
        sbrt.append("Hello");
        sbrt.append(" ");
        sbrt.append("World");
        sbrt.append("!");
        StringBuilder b = sbrt.getBuilder();
        String str = b.toString();
        System.out.println(str);
    }

}

