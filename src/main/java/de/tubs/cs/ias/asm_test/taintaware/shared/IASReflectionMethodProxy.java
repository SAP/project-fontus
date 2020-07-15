package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.taintaware.bool.IASMatcher;
import de.tubs.cs.ias.asm_test.taintaware.bool.IASPattern;
import de.tubs.cs.ias.asm_test.utils.JdkClassesLookupTable;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
public class IASReflectionMethodProxy {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();
    private static final String packageName = "de.tubs.cs.ias.asm_test.taintaware.";
    private static final Map<String, Class<?>> toInterfaceReplacements = new HashMap<>();
    private static final Map<String, Class<?>> toOrigReplacements = new HashMap<>();
    private static final Map<String, MethodHandle> toOrigMethods = new HashMap<>();
    private static final Map<Class, Function<Object, Object>> toTaintedMethods = new HashMap<>();

    static {
        toInterfaceReplacements.put("IASString", IASStringable.class);
        toInterfaceReplacements.put("IASStringBuilder", IASStringBuilderable.class);
        toInterfaceReplacements.put("IASStringBuffer", IASStringBuilderable.class);
        toInterfaceReplacements.put("IASFormatter", IASFormatterable.class);

        toOrigReplacements.put("IASString", String.class);
        toOrigReplacements.put("IASStringBuilder", StringBuilder.class);
        toOrigReplacements.put("IASStringBuffer", StringBuffer.class);
        toOrigReplacements.put("IASFormatter", Formatter.class);
        toOrigReplacements.put("IASPattern", IASPattern.class);
        toOrigReplacements.put("IASMatcher", IASMatcher.class);

        try {
            toOrigMethods.put("IASString", lookup.findVirtual(IASStringable.class, "getString", MethodType.methodType(String.class)));
            toOrigMethods.put("IASStringBuilder", lookup.findVirtual(IASStringBuilderable.class, "getStringBuilder", MethodType.methodType(StringBuilder.class)));
            toOrigMethods.put("IASStringBuffer", lookup.findVirtual(IASStringBuilderable.class, "getStringBuffer", MethodType.methodType(StringBuffer.class)));
            toOrigMethods.put("IASFormatter", lookup.findVirtual(IASFormatterable.class, "getFormatter", MethodType.methodType(Formatter.class)));
            toOrigMethods.put("IASPattern", lookup.findVirtual(IASPatternable.class, "getPattern", MethodType.methodType(Pattern.class)));
            toOrigMethods.put("IASMatcher", lookup.findVirtual(IASMatcherable.class, "getMatcher", MethodType.methodType(Matcher.class)));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        toTaintedMethods.put(String.class, (param) -> factory.createString((String) param));
        toTaintedMethods.put(StringBuilder.class, (param) -> factory.createStringBuilder((StringBuilder) param));
        toTaintedMethods.put(StringBuffer.class, (param) -> factory.createStringBuffer((StringBuffer) param));
        toTaintedMethods.put(Formatter.class, (param) -> factory.createFormatter((Formatter) param));
        toTaintedMethods.put(Pattern.class, (param) -> factory.createPattern((Pattern) param));
        toTaintedMethods.put(Matcher.class, (param) -> factory.createMatcher((Matcher) param));
    }

    public static Object handleInvocationProxyCall(Object result, Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass().isAnnotation() && method.getReturnType().equals(String.class)) {
            if (result instanceof IASStringable) {
                return ((IASStringable) result).getString();
            }
        }
        return result;
    }

    public static Object invoke(Method method, Object instance, Object... parameters) throws Throwable {
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
        } else if (isJdkClass(method.getDeclaringClass())) {
            Object[] converted = convertParametersToOriginal(parameters);
            Object result = method.invoke(instance, converted);
            return convertResultToTainted(method.getReturnType(), result);
        }
        return method.invoke(instance, parameters);
    }

    private static Object[] convertParametersToOriginal(Object[] parameters) throws Throwable {
        if (parameters == null) {
            return null;
        }
        Object[] converted = new Object[parameters.length];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = convertParameterToOriginal(parameters[i].getClass(), parameters[i]);
        }
        return converted;
    }

    private static Object convertParameterToOriginal(Class parameterType, Object param) throws Throwable {
        if (param == null) {
            return null;
        }
        for (String cls : toOrigMethods.keySet()) {
            if (isReplacable(parameterType, cls)) {
                return toOrigMethods.get(cls).invoke(param);
            }
        }
        return param;
    }

    private static Object convertResultToTainted(Class resultType, Object result) {
        if (result == null) {
            return null;
        }
        for (Class instrumentable : toTaintedMethods.keySet()) {
            if (instrumentable.equals(resultType)) {
                return toTaintedMethods.get(instrumentable).apply(result);
            }
        }
        return result;
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

    private static boolean isJdkClass(Class cls) {
        return JdkClassesLookupTable.getInstance().isJdkClass(cls);
    }

    /**
     * Proxy for the Class.getMethod function.
     * If the method is taintaware class method, it replaces taintaware parameter types with the interface type
     * If the method is JDK class method, it replaces taintaware parameter types with the original type
     * If the method is toString of an application class, it is proxied to the $toString method
     */
    public static Method getMethodProxied(Class<?> clazz, IASStringable methodName, Class[] parameters) throws NoSuchMethodException {
        String methodNameString = transformMethodName(clazz, methodName.getString(), parameters);

        if (JdkClassesLookupTable.getInstance().isJdkClass(clazz)) {
            parameters = transformParametersForJdk(parameters);
        } else if (isInPackage(clazz)) {
            parameters = transformParametersForTaintawareInterface(parameters);
        }

        return clazz.getMethod(methodNameString, parameters);
    }

    private static Class[] transformParametersForJdk(Class[] parameters) {
        return transformParameters(parameters, toOrigReplacements);
    }

    private static Class[] transformParametersForTaintawareInterface(Class[] parameters) {
        return transformParameters(parameters, toInterfaceReplacements);
    }

    private static Class[] transformParameters(Class[] parameters, Map<String, Class<?>> toOrigReplacements) {
        if (parameters == null) {
            return null;
        }
        Class[] classes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class cls = parameters[i];
            if (isInPackage(cls)) {
                classes[i] = replaceParameter(cls, toOrigReplacements);
            } else {
                classes[i] = cls;
            }
        }
        return classes;
    }

    /**
     * Proxy for the Class.getDeclaredMethod function.
     * Behaves like {@link IASReflectionMethodProxy#getMethodProxied(Class, IASStringable, Class[])},
     * but uses getDeclared method instead of getMethod
     */
    public static Method getDeclaredMethodProxied(Class<?> clazz, IASStringable methodName, Class[] parameters) throws NoSuchMethodException {
        String methodNameString = transformMethodName(clazz, methodName.getString(), parameters);

        if (JdkClassesLookupTable.getInstance().isJdkClass(clazz)) {
            parameters = transformParametersForJdk(parameters);
        } else if (isInPackage(clazz)) {
            parameters = transformParametersForTaintawareInterface(parameters);
        }

        return clazz.getDeclaredMethod(methodNameString, parameters);
    }

    private static String transformMethodName(Class<?> clazz, String methodName, Class[] parameters) {
        boolean isJdk = JdkClassesLookupTable.getInstance().isJdkClass(clazz);
        boolean isToString = methodName.equals(Constants.ToString) && (parameters == null || parameters.length == 0);
        boolean isTaintAware = IASTaintAware.class.isAssignableFrom(clazz);


        if (!isJdk && !isInPackage(clazz) && isToString) {
            return Constants.ToStringInstrumented;
        } else if (isTaintAware && isToString) {
            return Constants.TO_TSTRING;
        }

        return methodName;
    }

    private static Class replaceParameter(Class parameter, Map<String, Class<?>> replacements) {
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
