class InnerClassTest {

    private int value;

    public InnerClassTest(int v) {
        this.value = v;
    }

    public int getValue() { return this.value; }

    class InnerInner {

        private String val;

        public InnerInner(String vv) {
            this.val = vv;
            this.val += value;
            value++;
        }

        String getVal() { return this.val; }

    }

    public static void main(String[] args) {

        InnerClassTest ict = new InnerClassTest(5);
        System.out.println("ICT value: " + ict.getValue());
        InnerClassTest.InnerInner ii = ict.new InnerInner("test");
        System.out.println("II val: " + ii.getVal());
        System.out.println("ICT value #2: " + ict.getValue());
    }

}
