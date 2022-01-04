import java.lang.reflect.*;

class ReflectionUtils {
    static <T> T makeInstance(Class<T> type) throws Exception {
        Constructor<T> c = newConstructorForSerialization(type, getJavaLangObjectConstructor());
        c.setAccessible(true);
        T t = c.newInstance((Object[]) null);
        return t;
    }
   static Constructor<Object> getJavaLangObjectConstructor() {
      try {
         return Object.class.getConstructor((Class[]) null);
      }
      catch(NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }
   static <T> Constructor<T> newConstructorForSerialization(Class<T> type,
      Constructor<?> constructor) {
      Class<?> reflectionFactoryClass = getReflectionFactoryClass();
      Object reflectionFactory = createReflectionFactory(reflectionFactoryClass);

      Method newConstructorForSerializationMethod = getNewConstructorForSerializationMethod(
         reflectionFactoryClass);

      try {
         return (Constructor<T>) newConstructorForSerializationMethod.invoke(
            reflectionFactory, type, constructor);
      }
      catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
         throw new RuntimeException(e);
      }
   }

    static Class<?> getReflectionFactoryClass() {
      try {
         return Class.forName("sun.reflect.ReflectionFactory");
      }
      catch(ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }

    static Object createReflectionFactory(Class<?> reflectionFactoryClass) {
      try {
         Method method = reflectionFactoryClass.getDeclaredMethod(
            "getReflectionFactory");
         return method.invoke(null);
      }
      catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
         throw new RuntimeException(e);
      }
   }

   static Method getNewConstructorForSerializationMethod(Class<?> reflectionFactoryClass) {
      try {
         return reflectionFactoryClass.getDeclaredMethod(
            "newConstructorForSerialization", Class.class, Constructor.class);
      }
      catch(NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

}
