package de.tubs.cs.ias.asm_test.taintaware.bool;

public class StringTest {
    public static void main(String[] args) {
        testInstrumentation();
    }

    public static void testInstrumentation() {
        String s1 = "Hallo ";
        String s2 = "Welt";

        Object s = s1.concat(s2);
        System.out.println(s);

        StringBuilder sb = new StringBuilder(s1);
        sb.append(s2);
        System.out.println(sb.toString());

        System.out.println(s.getClass());
        System.out.println(sb.getClass());
    }
}
