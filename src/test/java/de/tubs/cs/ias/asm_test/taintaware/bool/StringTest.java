package de.tubs.cs.ias.asm_test.taintaware.bool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StringTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        testInstrumentationConcat();
//        testInstrumentationStartsWith();
//        testInstrumentationNull2(null);
        testReflection();
    }

    public static void testInstrumentationConcat() {
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

    public static void testInstrumentationStartsWith() {
        String s1 = "Hallo ";
        String s2 = "Hal";

        boolean sw = s1.startsWith(s2);
        System.out.println(sw);

        System.out.println(s1.getClass());
    }

    public static void testInstrumentationNull2(String s) {
        System.out.println(s + 1);
        System.out.println(getString(s + 1));
    }

    public static void testArray() {
        Class<?> clz = Object[].class;
    }

    public static String getString(String s) {
        return null;
    }

    public static void testReflection() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> string = Class.forName("java.lang.String");
        Method m = string.getMethod("concat", String.class);
//        m = string.getDeclaredMethod("concat", String.class);
        String res = (String) m.invoke("Hallo ", "Welt");
        System.out.println(res);
    }
}
