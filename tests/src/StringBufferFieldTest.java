class StringBufferFieldTest {

    private StringBuffer buffer;

    StringBufferFieldTest() {
        this.buffer = new StringBuffer();
    }

    StringBufferFieldTest(String init) {
        this.buffer = new StringBuffer(init);
    }

    StringBufferFieldTest(int capacity) {
        this.buffer = new StringBuffer(capacity);
    }

    void append(String txt) {
        this.buffer.append(txt);
    }

    void append(int num) {
        this.buffer.append(num);
    }

    void append(char c) {
        this.buffer.append(c);
    }

    void append(float fl) {
        this.buffer.append(fl);
    }

    public static void main(String[] args) {
        StringBufferFieldTest[] buffers = {
            new StringBufferFieldTest(),
            new StringBufferFieldTest("Hello"),
            new StringBufferFieldTest(16)
        };

        for(StringBufferFieldTest b : buffers) {
            b.append(',');
            b.append(' ');
        }

        for(StringBufferFieldTest b : buffers) {
            b.append(1337);
        }

        for(StringBufferFieldTest b : buffers) {
            b.append(' ');
            b.append("world");
        }

        String s1 = buffers[0].toString();
        String s2 = buffers[1].toString();
        String s3 = buffers[2].toString();

        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
    }


}
