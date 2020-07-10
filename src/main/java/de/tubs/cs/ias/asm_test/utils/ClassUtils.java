package de.tubs.cs.ias.asm_test.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUtils {
    /**
     * Returns for the given class all instance methods which are public or protected
     * This includes the inherited ones (unlike getDeclaredMethods), but excludes the Object-class methods
     *
     * @param classToDiscover Class to discover
     * @return Discovered methods
     */
    public static List<Method> getAllMethods(Class<?> classToDiscover) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> cls = classToDiscover; !cls.equals(Object.class); cls = cls.getSuperclass()) {
            Method[] declaredMethods = cls.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                if (isPublicOrProtectedNotStatic(declaredMethod)) {
                    boolean alreadyContained = methods.stream().anyMatch(method -> {
                        boolean nameEquals = declaredMethod.getName().equals(method.getName());
                        boolean correctVisibility = isPublicOrProtectedNotStatic(method);
                        boolean signaturEquals = Arrays.equals(declaredMethod.getParameterTypes(), method.getParameterTypes());
                        return nameEquals && correctVisibility && signaturEquals;
                    });

                    if (!alreadyContained) {
                        methods.add(declaredMethod);
                    }
                }
            }
        }
        return methods;
    }

    public static boolean isPublicOrProtectedNotStatic(Method m) {
        return (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) && !Modifier.isStatic(m.getModifiers());
    }

    public static boolean hasGenericInformation(Method m) {
        try {
            Method hasGenericInformation = Method.class.getDeclaredMethod("hasGenericInformation");
            hasGenericInformation.setAccessible(true);
            return (boolean) hasGenericInformation.invoke(m);
        } catch (Exception ex) {
            throw new UnsupportedOperationException("Cannot evaluate if method is generic signature, because Method.hasGenericInformation is not available");
        }
    }
}
