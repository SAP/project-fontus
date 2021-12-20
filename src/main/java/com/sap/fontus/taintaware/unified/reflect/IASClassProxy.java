package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.Constants;
import com.sap.fontus.asm.Descriptor;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.IASInstrumenterInputStream;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.reflect.type.IASTypeVariableImpl;
import com.sap.fontus.utils.ConversionUtils;
import com.sap.fontus.utils.ReflectionUtils;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

public class IASClassProxy {
    private static final InstrumentationHelper instrumentationHelper = new InstrumentationHelper();
    private final static CombinedExcludedLookup lookup = new CombinedExcludedLookup();

    public static TypeVariable<Class>[] getTypeParameters(Class cls) {
        return Arrays.stream(cls.getTypeParameters()).map(IASTypeVariableImpl<Class>::new).toArray(IASTypeVariableImpl[]::new);
    }

    public static Class<?>[] getInterfaces(Class cls) {
        return Arrays.stream(cls.getInterfaces()).map(ConversionUtils::convertClassToConcrete).toArray(Class[]::new);
    }

    public static Type[] getGenericInterfaces(Class cls) {
        return Arrays.stream(cls.getGenericInterfaces()).map(ConversionUtils::convertTypeToInstrumented).toArray(Type[]::new);
    }

    public static IASMethod getEnclosingMethod(Class cls) throws SecurityException {
        return IASReflectRegistry.getInstance().map(cls.getEnclosingMethod());
    }

    public static IASConstructor<?> getEnclosingConstructor(Class cls) throws SecurityException {
        return IASReflectRegistry.getInstance().map(cls.getEnclosingConstructor());
    }

    public static IASField[] getFields(Class cls) throws SecurityException {
        return Arrays.stream(cls.getFields()).map(IASReflectRegistry.getInstance()::map).toArray(IASField[]::new);
    }

    public static IASMethod[] getMethods(Class cls) throws SecurityException {
        return filterFontusProxies(cls.getMethods())
                .stream()
                .map(IASReflectRegistry.getInstance()::map)
                .toArray(IASMethod[]::new);
    }

    private static <T extends Executable> List<T> filterFontusProxies(T[] methods) {
        List<T> filtered = new ArrayList<>(methods.length);
        for (T method : methods) {
            Descriptor descriptor = Descriptor.parseExecutable(method);
            Descriptor instrumented = instrumentationHelper.instrument(descriptor);
            if (!descriptor.equals(instrumented)) {
                Optional<T> instrumentation = Arrays.stream(methods)
                        .filter((m) -> m.getName().equals(method.getName()))
                        .filter((m) -> m.getParameterCount() == method.getParameterCount())
                        .filter((m) -> instrumented.equals(Descriptor.parseExecutable(m)))
                        .findFirst();
                if (instrumentation.isPresent()) {
                    continue;
                }
            }
            filtered.add(method);
        }
        return filtered;
    }

    public static <T> IASConstructor<T>[] getConstructors(Class<T> cls) throws SecurityException {
        return filterFontusProxies(cls.getConstructors()).stream().map(IASReflectRegistry.getInstance()::map).toArray(IASConstructor[]::new);
    }

    public static IASField getField(Class cls, IASString name) throws NoSuchFieldException, SecurityException {
        return IASReflectRegistry.getInstance().map(cls.getField(name.getString()));
    }

    public static IASField[] getDeclaredFields(Class cls) throws SecurityException {
        return Arrays.stream(cls.getDeclaredFields()).map(IASReflectRegistry.getInstance()::map).toArray(IASField[]::new);
    }

    public static IASMethod[] getDeclaredMethods(Class cls) throws SecurityException {
        return filterFontusProxies(cls.getDeclaredMethods()).stream().map(IASReflectRegistry.getInstance()::map).toArray(IASMethod[]::new);
    }

    public static <T> IASConstructor<T>[] getDeclaredConstructors(Class<T> cls) throws SecurityException {
        return filterFontusProxies(cls.getDeclaredConstructors()).stream().map(IASReflectRegistry.getInstance()::map).toArray(IASConstructor[]::new);
    }

    public static IASField getDeclaredField(Class cls, IASString name) throws NoSuchFieldException, SecurityException {
        return IASReflectRegistry.getInstance().map(cls.getDeclaredField(name.getString()));
    }

    public static InputStream getResourceAsStream(Class cls, IASString name) {
        InputStream stream = cls.getResourceAsStream(name.getString());
        if (Configuration.getConfiguration().isResourceToInstrument(name.getString())) {
            return new IASInstrumenterInputStream(stream);
        }
        return stream;
    }

    public static <T> IASConstructor<T> getDeclaredConstructor(Class<T> clazz, Class[] parameterTypes) throws NoSuchMethodException, SecurityException {
        if (lookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameterTypes = transformParametersForJdk(parameterTypes);
        } else if (lookup.isFontusClass(clazz)) {
            parameterTypes = transformParametersForInstrumentedWithConcrete(parameterTypes);
        }

        return IASReflectRegistry.getInstance().map(clazz.getDeclaredConstructor(parameterTypes));
    }

    public static IASMethod getDeclaredMethod(Class<?> clazz, IASString name, Class[] parameters) throws NoSuchMethodException, SecurityException {
        String methodNameString = transformMethodName(clazz, name.getString(), parameters);

        if (lookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameters = transformParametersForJdk(parameters);
        } else {
            parameters = transformParametersForInstrumentedWithConcrete(parameters);
        }

        return IASReflectRegistry.getInstance().map(clazz.getDeclaredMethod(methodNameString, parameters));
    }

    public static <T> IASConstructor<T> getConstructor(Class<T> clazz, Class[] parameters) throws NoSuchMethodException, SecurityException {
        if (lookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameters = transformParametersForJdk(parameters);
        } else {
            parameters = transformParametersForInstrumentedWithConcrete(parameters);
        }

        return IASReflectRegistry.getInstance().map(clazz.getConstructor(parameters));
    }

    public static IASMethod getMethod(Class<?> clazz, IASString name, Class[] parameters) throws NoSuchMethodException, SecurityException {
        String methodNameString = transformMethodName(clazz, name.getString(), parameters);

        if (lookup.isPackageExcludedOrJdk(Utils.getInternalName(clazz))) {
            parameters = transformParametersForJdk(parameters);
        } else {
            parameters = transformParametersForInstrumentedWithConcrete(parameters);
        }

        return IASReflectRegistry.getInstance().map(clazz.getMethod(methodNameString, parameters));
    }

    static Class[] transformParametersForJdk(Class[] parameters) {
        return transformParameters(parameters, ConversionUtils::convertClassToOrig);
    }

    static Class[] transformParametersForInstrumentedWithConcrete(Class[] parameters) {
        return transformParameters(parameters, ConversionUtils::convertClassToConcrete);
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

    private static String transformMethodName(Class<?> clazz, String methodName, Class[] parameters) {
        boolean isToString = methodName.equals(Constants.ToString) && (parameters == null || parameters.length == 0);
        boolean isTaintAware = IASTaintAware.class.isAssignableFrom(clazz);

        if (isTaintAware && isToString) {
            return Constants.TO_TSTRING;
        }

        return methodName;
    }

    public static class MethodSorter implements Comparator<Method> {
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
    }

    public static Class uninstrumentClass(Class clazz, ClassLoader classLoader) {
        org.objectweb.asm.Type originalType =
                org.objectweb.asm.Type.getObjectType(
                        instrumentationHelper.uninstrumentQN(org.objectweb.asm.Type.getType(clazz).getInternalName()));

        try {
            if (originalType.getSort() == org.objectweb.asm.Type.ARRAY) {
                org.objectweb.asm.Type elementType = originalType.getElementType();
                int[] dimensions = new int[originalType.getDimensions()];
                clazz = Array.newInstance(Class.forName(elementType.getClassName(), true, classLoader), dimensions).getClass();
            } else {
                clazz = Class.forName(originalType.getClassName(), true, classLoader);
            }
        } catch (ClassNotFoundException e) {
            // NOP
        }
        return clazz;
    }

    @SuppressWarnings("Since15")
    public static IASString getSimpleName(Class<?> clazz) {
        // Get caller class classloader
        Class<?> callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = ReflectionUtils.getCallerClass();
        }
        ClassLoader cl = callerClass.getClassLoader();
        return new IASString(uninstrumentClass(clazz, cl).getSimpleName());
    }

    @SuppressWarnings("Since15")
    public static IASString getCanonicalName(Class<?> clazz) {
        // Get caller class classloader
        Class<?> callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = ReflectionUtils.getCallerClass();
        }
        ClassLoader cl = callerClass.getClassLoader();
        return new IASString(uninstrumentClass(clazz, cl).getCanonicalName());
    }

    @SuppressWarnings("Since15")
    public static IASString getName(Class<?> clazz) {
        // Get caller class classloader
        Class<?> callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = ReflectionUtils.getCallerClass();
        }
        ClassLoader cl = callerClass.getClassLoader();
        return new IASString(uninstrumentClass(clazz, cl).getName());
    }

    @SuppressWarnings("Since15")
    public static Class<?> forName(IASString str) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = instrumentationHelper.translateClassName(s).orElse(s);

        // Get caller class classloader
        Class<?> callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = ReflectionUtils.getCallerClass();
        }
        ClassLoader cl = callerClass.getClassLoader();

        return Class.forName(clazz, true, cl);
    }

    @SuppressWarnings("Since15")
    public static Class<?> forName(IASString str, boolean initialize,
                                   ClassLoader loader) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = instrumentationHelper.translateClassName(s).orElse(s);

        if (loader == null) {
            // Get caller class classloader
            Class<?> callerClass;
            if (Constants.JAVA_VERSION >= 9) {
                callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .getCallerClass();
            } else {
                callerClass = ReflectionUtils.getCallerClass();
            }
            loader = callerClass.getClassLoader();
        }

        return Class.forName(clazz, initialize, loader);
    }
}

