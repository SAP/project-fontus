class StringViaStringBuffer {

    public static void main(String[] args) {
        String s = new String("hello");
        StringBuffer t = new StringBuffer(s);
        String u = new String(t);
        System.out.println(u);
    }
}
