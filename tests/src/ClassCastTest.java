class ClassCastTest {

    public static void main(String[] args) {

        String s1 = new String("hello");
        StringBuffer sb1 = new StringBuffer("hello");

        StringBuffer sb2 = StringBuffer.class.cast(sb1);
        String s2 = String.class.cast(s1);
        System.out.println("sb1: " + sb1 + " sb2: " + sb2);
        System.out.println("s1: " + s1 + " s2: " + s2);

    }

}
