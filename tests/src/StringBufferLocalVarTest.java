class StringBufferLocalVarTest {

    StringBufferLocalVarTest() {
    }

    String concat(String... strs) {
        StringBuffer b = new StringBuffer();
        for(String s : strs) {
            b.append(s);
        }
        return b.toString();
    }

    public static void main(String[] args) {
        StringBufferLocalVarTest sblvt = new StringBufferLocalVarTest();
        String[] txts = { "Hello", ", ", "world", "!" };
        String r = sblvt.concat(txts);
        System.out.println(r);
    }

}
