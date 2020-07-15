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
                addMethodIfNotContained(declaredMethod, methods);
            }
        }
        return methods;
    }

    /**
     * This methods add all methods if the passed interface list to the method list, if the method isn't already contained
     * For determination if contained see {@link ClassUtils#addMethodIfNotContained(Method, List)}
     * @param interfaces Array with interface names as QN
     * @param methods List to add methods (may already contain methods)
     */
    public static void addNotContainedJdkInterfaceMethods(String[] interfaces, List<Method> methods) {
        if (interfaces == null) {
            return;
        }
        for (String interfaceName : interfaces) {
            if (JdkClassesLookupTable.getInstance().isJdkClass(interfaceName)) {
                try {
                    Class cls = Class.forName(Utils.fixup(interfaceName));
                    for (Method m : cls.getMethods()) {
                        addMethodIfNotContained(m, methods);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Adds the passed method to the list, if it's not already contained.
     * The passed method must be an overridable method (public or protected and not never static)
     *
     * If a method is already contained is determined by the method name and descriptor (declaring class is NOT considered)
     */
    private static void addMethodIfNotContained(Method methodToAdd, List<Method> methods) {
        if (isPublicOrProtectedNotStatic(methodToAdd)) {
            boolean alreadyContained = methods.stream().anyMatch(methodInMethods -> {
                boolean nameEquals = methodToAdd.getName().equals(methodInMethods.getName());
                boolean correctVisibility = isPublicOrProtectedNotStatic(methodInMethods);
                boolean signaturEquals = Arrays.equals(methodToAdd.getParameterTypes(), methodInMethods.getParameterTypes());
                return nameEquals && correctVisibility && signaturEquals;
            });

            if (!alreadyContained) {
                methods.add(methodToAdd);
            }
        }
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
