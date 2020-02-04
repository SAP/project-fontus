class InternTest {

    public static void main(String[] args) {
        String s1 = new String("sup");
        String s2 = s1.intern();
        System.out.println("Equal? " + s1 == s2);
    }

}
