import java.lang.reflect.*;

class ReflectionTest2 {

    public static void main(String[] args) {
        try {
            Class c = Class.forName(args[0], true, ReflectionTest2.class.getClassLoader());
            Method m[] = c.getDeclaredMethods();
            for (int i = 0; i < m.length; i++)
            System.out.println(m[i].toString());
         }
         catch (Throwable e) {
            System.err.println(e);
         }
    }

}
