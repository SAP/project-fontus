package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.utils.JdkClassesLookupTable;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IASReflectionMethodProxy {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();
    private static final String packageName = "de.tubs.cs.ias.asm_test.taintaware.";
    private static final Map<String, Class<?>> replacements = new HashMap<>();

    static {
        replacements.put("IASString", IASStringable.class);
        replacements.put("IASStringBuilder", IASStringBuilderable.class);
        replacements.put("IASStringBuffer", IASStringBuilderable.class);
        replacements.put("IASFormatter", IASFormatterable.class);
    }

    public static Object invoke(Method method, Object instance, Object... parameters) throws InvocationTargetException, IllegalAccessException {
        if (method.getDeclaringClass().isAnnotation()) {
            if (method.getReturnType().isAssignableFrom(String.class)) {
                String result = (String) method.invoke(instance, parameters);
                IASStringable converted = factory.createString(result);
                return factory.getStringClass().cast(converted);
            } else if (method.getReturnType().isArray() && method.getReturnType().getComponentType().isAssignableFrom(String.class)) {
                String[] result = (String[]) method.invoke(instance, parameters);
                IASStringable[] converted = IASStringUtils.convertStringArray(result);
                return Arrays.copyOf(converted, converted.length, factory.getStringArrayClass());
            }
        }
        return method.invoke(instance, parameters);
    }

    public static Object getDefaultValue(Method method) {
        if (method.getDeclaringClass().isAnnotation()) {
            if (method.getReturnType().isAssignableFrom(String.class)) {
                String result = (String) method.getDefaultValue();
                IASStringable converted = factory.createString(result);
                return factory.getStringClass().cast(converted);
            } else if (method.getReturnType().isArray() && method.getReturnType().getComponentType().isAssignableFrom(String.class)) {
                String[] result = (String[]) method.getDefaultValue();
                IASStringable[] converted = IASStringUtils.convertStringArray(result);
                return Arrays.copyOf(converted, converted.length, factory.getStringArrayClass());
            }
        }
        return method.getDefaultValue();
    }

    public static Method getMethodProxied(Class<?> clazz, IASStringable methodName, Class[] parameters) throws NoSuchMethodException {
        parameters = transformParameters(parameters);
        String methodNameString = transformMethodName(clazz, methodName.getString(), parameters);
        return clazz.getMethod(methodNameString, parameters);
    }

    private static String transformMethodName(Class<?> clazz, String methodName, Class[] parameters) {
        boolean isJdk = JdkClassesLookupTable.getInstance().isJdkClass(clazz.getName().replace('.', '/'));
        boolean isToString = methodName.equals(Constants.ToString) && (parameters == null || parameters.length == 0);
        boolean isTaintAware = IASTaintAware.class.isAssignableFrom(clazz);


        if (!isJdk && !isInPackage(clazz) && isToString) {
            return Constants.ToStringInstrumented;
        } else if (isTaintAware && isToString) {
            return Constants.TO_TSTRING;
        }

        return methodName;
    }

    private static Class[] transformParameters(Class[] parameters) {
        if (parameters == null) {
            return null;
        }

        Class[] classes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class parameter = parameters[i];
            if (isInPackage(parameter)) {
                classes[i] = replaceParameter(parameter);
            } else {
                classes[i] = parameter;
            }
        }
        return classes;
    }

    public static Method getDeclaredMethodProxied(Class<?> clazz, IASStringable methodName, Class[] parameters) throws NoSuchMethodException {
        parameters = transformParameters(parameters);
        String methodNameString = transformMethodName(clazz, methodName.getString(), parameters);
        return clazz.getDeclaredMethod(methodNameString, parameters);
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

    private static boolean isInPackage(Class clazz) {
        return clazz.getName().startsWith(packageName);
    }
}
