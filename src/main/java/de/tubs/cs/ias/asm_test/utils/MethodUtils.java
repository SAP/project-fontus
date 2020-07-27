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
            exceptions[i] = Utils.fixupReverse(ex.getType().getTypeName());
            i++;
        }
        return exceptions;
    }
}
