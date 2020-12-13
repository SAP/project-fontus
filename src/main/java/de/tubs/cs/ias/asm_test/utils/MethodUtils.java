package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.Constants;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;

public class MethodUtils {
    public static boolean isToString(String name, String methodDescriptor) {
        return "()Ljava/lang/String;".equals(methodDescriptor) && Constants.ToString.equals(name);
    }

    public static String[] getExceptionTypes(Method method) {
        AnnotatedType[] annotatedExceptionTypes = method.getAnnotatedExceptionTypes();
        String[] exceptions = new String[annotatedExceptionTypes.length];
        int i = 0;
        for (AnnotatedType ex : annotatedExceptionTypes) {
            exceptions[i] = Utils.dotToSlash(ex.getType().getTypeName());
            i++;
        }
        return exceptions;
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
