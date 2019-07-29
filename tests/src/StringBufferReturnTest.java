class StringBufferReturnTest {

    private StringBuffer buf;

    public  StringBufferReturnTest() {
        this.buf = new StringBuffer(16);
    }

    public void append(String str) {
        this.buf.append(str);
    }

    public StringBuffer getBuffer() {
        return this.buf;
    }

    public static void main(String[] args) {
        StringBufferReturnTest sbrt = new StringBufferReturnTest();
        sbrt.append("Hello");
        sbrt.append(" ");
        sbrt.append("World");
        sbrt.append("!");
        StringBuffer b = sbrt.getBuffer();
        String str = b.toString();
        System.out.println(str);
    }

}
