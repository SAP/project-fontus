package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.taintaware.IASTaintAware;
import de.tubs.cs.ias.asm_test.taintaware.bool.IASMatcher;
import de.tubs.cs.ias.asm_test.taintaware.bool.IASPattern;
import de.tubs.cs.ias.asm_test.utils.ConversionUtils;
import de.tubs.cs.ias.asm_test.utils.JdkClassesLookupTable;
import jdk.internal.reflect.Reflection;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    private static final Map<Class, Function<Object, Object>> toTaintedMethods = new HashMap<>();

    static {
        toInterfaceReplacements.put("IASString", IASStringable.class);
        toInterfaceReplacements.put("IASStringBuilder", IASAbstractStringBuilderable.class);
        toInterfaceReplacements.put("IASStringBuffer", IASAbstractStringBuilderable.class);
        toInterfaceReplacements.put("IASFormatter", IASFormatterable.class);

        toOrigReplacements.put("IASString", String.class);
        toOrigReplacements.put("IASStringBuilder", StringBuilder.class);
        toOrigReplacements.put("IASStringBuffer", StringBuffer.class);
        toOrigReplacements.put("IASFormatter", Formatter.class);
        toOrigReplacements.put("IASPattern", IASPattern.class);
        toOrigReplacements.put("IASMatcher", IASMatcher.class);

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

    @SuppressWarnings("Since15")
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
            return ConversionUtils.convertToConcrete(result);
        }
        if ((!Modifier.isPublic(method.getModifiers()) && !Modifier.isProtected(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()))
                || (!Modifier.isPublic(method.getDeclaringClass().getModifiers()) && !Modifier.isProtected(method.getDeclaringClass().getModifiers()) && !Modifier.isPrivate(method.getDeclaringClass().getModifiers()))) {
            // This method is package private. Iuff the declaring class is in the same package as the calling class we must set it accessible
            // Otherwise the caller class (which is this class) is not in the same package as the declaring class an an IllegalAccessException is thrown
            Class callerClass;
            if (Constants.JAVA_VERSION >= 9) {
                callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .getCallerClass();
            } else {
                callerClass = Reflection.getCallerClass();
            }
            if (method.getDeclaringClass().getPackage().equals(callerClass.getPackage())) {
                method.setAccessible(true);
            }
        }
        return method.invoke(instance, parameters);
    }

    private static Object[] convertParametersToOriginal(Object[] parameters) throws Throwable {
        if (parameters == null) {
            return null;
        }
        Object[] converted = new Object[parameters.length];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = ConversionUtils.convertToOrig(parameters[i]);
        }
        return converted;
    }

    public static Object getDefaultValue(Method method) {
        if (method.getDeclaringClass().isAnnotation()) {
            if (method.getReturnType().isAssignableFrom(String.class)) {
                String result = (String) method.getDefaultValue();
                IASStringable converted = factory.createString(result);
                return factory.getStringClass().cast(converted);
            } else if (method.getReturnType().isArray() && method.getReturnType().getComponentType().isAssignableFrom(String.class)) {
                String[] result = (String[]) method.getDefaultValue();
                if (result == null) {
                    return null;
                }
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
        boolean isToString = methodName.equals(Constants.ToString) && (parameters == null || parameters.length == 0);
        boolean isTaintAware = IASTaintAware.class.isAssignableFrom(clazz);

        if (isTaintAware && isToString) {
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
