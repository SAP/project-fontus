import java.lang.annotation.Annotation;

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
        System.out.println(d.value());        
    }
    private static void printAnnotations(Object object) {
        Class<?> clazz = object.getClass();
        Annotation[] annotations = clazz.getAnnotations();
        for(Annotation a : annotations) {
            System.out.println(a.toString());
        }
    }

}
