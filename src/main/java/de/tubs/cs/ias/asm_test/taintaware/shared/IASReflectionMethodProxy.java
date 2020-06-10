package de.tubs.cs.ias.asm_test.taintaware.shared;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class IASReflectionMethodProxy {
    private static final String packageName = "de.tubs.cs.ias.asm_test.taintaware.";
    private static final Map<String, Class<?>> replacements = new HashMap<>();

    static {
        replacements.put("IASString", IASStringable.class);
        replacements.put("IASStringBuilder", IASStringBuilderable.class);
        replacements.put("IASStringBuffer", IASStringBuilderable.class);
        replacements.put("IASFormatter", IASFormatterable.class);
    }

    public static Method getMethodProxied(Class<?> clazz, IASStringable methodName, Class[] parameters) throws NoSuchMethodException {
        transformParameters(parameters);
        return clazz.getMethod(methodName.getString(), parameters);
    }

    private static void transformParameters(Class[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            Class parameter = parameters[i];
            if (isInPackage(parameter)) {
                parameters[i] = replaceParameter(parameter);
            }
        }
    }

    public static Method getDeclaredMethodProxied(Class<?> clazz, IASStringable methodName, Class[] parameters) throws NoSuchMethodException {
        transformParameters(parameters);
        return clazz.getDeclaredMethod(methodName.getString(), parameters);
    }

    private static Class replaceParameter(Class parameter) {
        for (String name : replacements.keySet()) {
            if (isReplacable(parameter, name)) {
                boolean isArray = parameter.isArray();
                parameter = replacements.get(name);
                if (isArray) {
                    parameter = Array.newInstance(parameter, 0).getClass();
                }
                break;
            }
        }
        return parameter;
    }

    private static boolean isReplacable(Class parameter, String name) {
        return parameter.getName().endsWith("." + name);
    }

    private static boolean isInPackage(Class parameter) {
        return parameter.getName().startsWith(packageName);
    }
}
