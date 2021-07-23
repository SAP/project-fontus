package com.sap.fontus.taintaware.shared;

import com.sap.fontus.Constants;
import com.sap.fontus.utils.ConversionUtils;
import com.sap.fontus.utils.ReflectionUtils;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class IASReflectionMethodProxy {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();
    private static final String packageName = "com.sap.fontus.taintaware.";
    private static final CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup();

    private static final Method forNameMethod;

    static {
        try {
            forNameMethod = Class.class.getMethod("forName", String.class);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not load method Class.forName");
            throw new RuntimeException(e);
        }
    }

    public static Object handleInvocationProxyCall(Object result, Object proxy, Method method, Object[] args) {
        if (method.getReturnType().equals(String.class) || method.getReturnType().equals(String[].class)) {
            if (result instanceof IASStringable) {
                return ((IASStringable) result).getString();
            } else if (result.getClass() == factory.getStringArrayClass()) {
                return ConversionUtils.convertToOrig(result);
            }
        }
        return result;
    }

    public static Class<?> getReturnType(Method m) {
        if (m.getDeclaringClass().isAnnotation()) {
            if (m.getReturnType() == String.class) {
                return factory.getStringClass();
            } else if (m.getReturnType() == String[].class) {
                return factory.getStringArrayClass();
            }
        }
        return m.getReturnType();
    }

    @SuppressWarnings("Since15")
    public static Object newInstance(Constructor constructor, Object[] parameters) throws Throwable {
        if (combinedExcludedLookup.isPackageExcludedOrJdk(Utils.getInternalName(constructor.getDeclaringClass()))) {
            Object[] converted = convertParametersToOriginal(parameters);
            return constructor.newInstance(converted);
        }
        if ((!Modifier.isPublic(constructor.getModifiers()) && !Modifier.isProtected(constructor.getModifiers()) && !Modifier.isPrivate(constructor.getModifiers()))
                || (!Modifier.isPublic(constructor.getDeclaringClass().getModifiers()) && !Modifier.isProtected(constructor.getDeclaringClass().getModifiers()) && !Modifier.isPrivate(constructor.getDeclaringClass().getModifiers()))) {
            // This method is package private. Iuff the declaring class is in the same package as the calling class we must set it accessible
            // Otherwise the caller class (which is this class) is not in the same package as the declaring class an an IllegalAccessException is thrown
            Class callerClass;
            if (Constants.JAVA_VERSION >= 9) {
                callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .getCallerClass();
            } else {
                callerClass = ReflectionUtils.getCallerClass();
            }
            if (constructor.getDeclaringClass().getPackage().equals(callerClass.getPackage())) {
                constructor.setAccessible(true);
            }
        }
        return constructor.newInstance(parameters);
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
        } else if (combinedExcludedLookup.isPackageExcludedOrJdk(Utils.getInternalName(method.getDeclaringClass()))) {
            Object[] converted = convertParametersToOriginal(parameters);

            if (method.equals(forNameMethod)) {
                Class caller = ReflectionUtils.getCallerClass();
                ClassLoader callerLoader = caller.getClassLoader();
                return Class.forName((String) converted[0], true, callerLoader);
            }

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
                callerClass = ReflectionUtils.getCallerClass();
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

    /**
     * Proxy for the Class.getMethod function.
     * If the method is taintaware class method, it replaces taintaware parameter types with the interface type
     * If the method is JDK class method, it replaces taintaware parameter types with the original type
     * If the method is toString of an application class, it is proxied to the $toString method
     */
    public static Method getMethodProxied(Class<?> clazz, IASStringable methodName, Class[] parameters) throws NoSuchMethodException {
        String methodNameString = transformMethodName(clazz, methodName.getString(), parameters);

        if (combinedExcludedLookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameters = transformParametersForJdk(parameters);
        } else if (isInPackage(clazz)) {
            parameters = transformParametersForTaintawareInterface(parameters);
        } else {
            parameters = transformParametersForInstrumentedWithConcrete(parameters);
        }

        return clazz.getMethod(methodNameString, parameters);
    }

    /**
     * Proxy for the Class.getConstructor function.
     * If the method is JDK class method, it replaces taintaware parameter types with the original type
     */
    public static Constructor getConstructor(Class<?> clazz, Class[] parameters) throws NoSuchMethodException {
        if (combinedExcludedLookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameters = transformParametersForJdk(parameters);
        } else if (isInPackage(clazz)) {
            parameters = transformParametersForTaintawareInterface(parameters);
        }

        return clazz.getConstructor(parameters);
    }

    /**
     * Proxy for the Class.getConstructor function.
     * If the method is JDK class method, it replaces taintaware parameter types with the original type
     */
    public static Constructor getDeclaredConstructor(Class<?> clazz, Class[] parameters) throws NoSuchMethodException {
        if (combinedExcludedLookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameters = transformParametersForJdk(parameters);
        } else if (isInPackage(clazz)) {
            parameters = transformParametersForTaintawareInterface(parameters);
        }

        return clazz.getDeclaredConstructor(parameters);
    }

    static Class[] transformParametersForJdk(Class[] parameters) {
        return transformParameters(parameters, ConversionUtils::convertClassToOrig);
    }

    static Class[] transformParametersForInstrumentedWithConcrete(Class[] parameters) {
        return transformParameters(parameters, ConversionUtils::convertClassToConcrete);
    }

    static Class[] transformParametersForTaintawareInterface(Class[] parameters) {
        return transformParameters(parameters, ConversionUtils::convertClassToInterface);
    }

    private static Class[] transformParameters(Class[] parameters, Function<Class<?>, Class<?>> converter) {
        if (parameters == null) {
            return null;
        }
        Class[] classes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class cls = parameters[i];
            classes[i] = converter.apply(cls);
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

        if (combinedExcludedLookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameters = transformParametersForJdk(parameters);
        } else if (isInPackage(clazz)) {
            parameters = transformParametersForTaintawareInterface(parameters);
        } else {
            parameters = transformParametersForInstrumentedWithConcrete(parameters);
        }

        return clazz.getDeclaredMethod(methodNameString, parameters);
    }

    public static Method[] getDeclaredMethods(Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, new MethodSorter());
        return methods;
    }

    public static Method[] getMethods(Class clazz) {
        Method[] methods = clazz.getMethods();
        Arrays.sort(methods, new MethodSorter());
        return methods;
    }

    public static InputStream getResourceAsStream(Class cls, IASStringable resource) {
        InputStream stream = cls.getResourceAsStream(resource.getString());
        if (Configuration.getConfiguration().isResourceToInstrument(resource.getString())) {
            return new IASInstrumenterInputStream(stream);
        }
        return stream;
    }

    public static InputStream getResourceAsStream(ClassLoader cls, IASStringable resource) {
        InputStream stream = cls.getResourceAsStream(resource.getString());
        if (Configuration.getConfiguration().isResourceToInstrument(resource.getString())) {
            return new IASInstrumenterInputStream(stream);
        }
        return stream;
    }

    private static boolean isParameterSameUninstrumented(Class c1, Class c2) {
        return ConversionUtils.convertClassToOrig(c1) == ConversionUtils.convertClassToOrig(c2);
    }

    private static boolean isInstrumented(Class cls) {
        if (cls.isPrimitive()) {
            return false;
        }
        if (cls.isArray()) {
            cls = cls.getComponentType();
        }
        return cls.getPackage().getName().startsWith(Constants.PACKAGE);
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
        return parameter.getName().endsWith("." + name) || parameter.getName().endsWith("." + name + ";");
    }

    private static boolean isInPackage(Class clazz) {
        return clazz.getName().contains(packageName);
    }

    private static class MethodSorter implements Comparator<Method> {

        @Override
        public int compare(Method o1, Method o2) {
            if (o1.getName().equals(o2.getName())) {
                if (o1.getParameterCount() == o2.getParameterCount()) {
                    boolean isSame = true;
                    for (int i = 0; i < o1.getParameterCount(); i++) {
                        if (!isParameterSameUninstrumented(o1.getParameterTypes()[i], o2.getParameterTypes()[i])) {
                            isSame = false;
                            break;
                        }
                    }
                    if (!isParameterSameUninstrumented(o1.getReturnType(), o2.getReturnType())) {
                        isSame = false;
                    }

                    if (isSame) {
                        boolean firstGreater = true;
                        for (int i = 0; i < o1.getParameterCount(); i++) {
                            if (isInstrumented(o2.getParameterTypes()[i])) {
                                firstGreater = false;
                                break;
                            }
                        }
                        if (isInstrumented(o2.getReturnType())) {
                            firstGreater = false;
                        }

                        return firstGreater ? -1 : 1;
                    }
                }
            }
            return 0;
        }
    }
}
