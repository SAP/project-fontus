import java.lang.annotation.Annotation;

import java.util.*;

class Main {

    public static void main(String[] args) {
        Entity e = new Entity("yo");
        printAnnotations(e);
        printDescriptor(e);
        System.out.println(e.getName());
    }

    private static void printDescriptor(Object object) {
        Class<?> clazz = object.getClass();
        Descriptor d = clazz.getDeclaredAnnotation(Descriptor.class);
        Class reflectedClass = null;
        try {
            reflectedClass = (Class)d.annotationType().getMethod("clazz").invoke(d);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println(d.value());
        System.out.println(d.name());
        System.out.println(d.age());
        System.out.println(Arrays.toString(d.newNames()));
        System.out.println("String class name: " + d.name().getClass().getName());
        System.out.println("Clazz attribute:   " + d.clazz().getName());
        System.out.println("Are they equal: " + d.clazz().equals(d.name().getClass()));
        System.out.println("Default Clazz attribute:   " + d.defaultClazz().getName());
        System.out.println("Are they equal: " + d.clazz().equals(d.name().getClass()));
        System.out.println("Clazz attribute via reflection:   " + reflectedClass.getName());
    }
    private static void printAnnotations(Object object) {
        Class<?> clazz = object.getClass();
        Annotation[] annotations = clazz.getAnnotations();
        for(Annotation a : annotations) {
            System.out.println(a.toString());
        }
    }

}
