package com.sap.fontus.utils;

import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.unified.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversionUtils {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private static final Map<Class<?>, Function<Object, Object>> toConcrete = new HashMap<>();
    private static final Map<Class<?>, Function<Object, Object>> toOrig = new HashMap<>();
    private static final Map<Class<?>, MethodHandle> toOrigMethods = new HashMap<>();
    private static final Map<Class<?>, MethodHandle> toConcreteMethods = new HashMap<>();
    private static final Map<Class<?>, Class<?>> toConcreteClass = new HashMap<>();
    private static final Map<Class<?>, Class<?>> toOrigClass = new HashMap<>();

    static {
        toConcrete.put(String.class, IASString::valueOf);
        toConcrete.put(StringBuilder.class, (obj) -> IASStringBuilder.fromStringBuilder((StringBuilder) obj));
        toConcrete.put(StringBuffer.class, (obj) -> IASStringBuffer.fromStringBuffer((StringBuffer) obj));
        toConcrete.put(Formatter.class, (obj) -> IASFormatter.fromFormatter((Formatter) obj));
        toConcrete.put(Matcher.class, (obj) -> IASMatcher.fromMatcher((Matcher) obj));
        toConcrete.put(Pattern.class, (obj) -> IASPattern.fromPattern((Pattern) obj));
        toConcrete.put(Properties.class, (obj) -> IASProperties.fromProperties((Properties) obj));

        toOrig.put(IASString.class, (obj) -> ((IASString) obj).getString());
        toOrig.put(IASStringBuilder.class, (obj) -> ((IASStringBuilder) obj).getStringBuilder());
        toOrig.put(IASStringBuffer.class, (obj) -> ((IASStringBuffer) obj).getStringBuffer());
        toOrig.put(IASFormatter.class, (obj) -> ((IASFormatter) obj).getFormatter());
        toOrig.put(IASMatcher.class, (obj) -> ((IASMatcher) obj).getMatcher());
        toOrig.put(IASPattern.class, (obj) -> ((IASPattern) obj).getPattern());
        toOrig.put(IASProperties.class, (obj) -> ((IASProperties) obj).getProperties());

        toOrigClass.put(IASString.class, String.class);
        toOrigClass.put(IASStringBuilder.class, StringBuilder.class);
        toOrigClass.put(IASStringBuffer.class, StringBuffer.class);
        toOrigClass.put(IASFormatter.class, Formatter.class);
        toOrigClass.put(IASMatcher.class, Matcher.class);
        toOrigClass.put(IASPattern.class, Pattern.class);
        toOrigClass.put(IASProperties.class, Properties.class);

        toConcreteClass.put(String.class, IASString.class);
        toConcreteClass.put(StringBuilder.class, IASStringBuilder.class);
        toConcreteClass.put(StringBuffer.class, IASStringBuffer.class);
        toConcreteClass.put(Formatter.class, IASFormatter.class);
        toConcreteClass.put(Matcher.class, IASMatcher.class);
        toConcreteClass.put(Pattern.class, IASPattern.class);
        toConcreteClass.put(Properties.class, IASProperties.class);

        try {
            toConcreteMethods.put(String.class, lookup.findConstructor(IASString.class, MethodType.methodType(void.class, String.class)));
            toConcreteMethods.put(StringBuilder.class, lookup.findConstructor(IASStringBuilder.class, MethodType.methodType(void.class, StringBuilder.class)));
            toConcreteMethods.put(StringBuffer.class, lookup.findConstructor(IASStringBuffer.class, MethodType.methodType(void.class, StringBuffer.class)));
            toConcreteMethods.put(Formatter.class, lookup.findConstructor(IASFormatter.class, MethodType.methodType(void.class, Formatter.class)));
            toConcreteMethods.put(Matcher.class, lookup.findConstructor(IASMatcher.class, MethodType.methodType(void.class, Matcher.class)));
            toConcreteMethods.put(Pattern.class, lookup.findConstructor(IASPattern.class, MethodType.methodType(void.class, Pattern.class)));
            toConcreteMethods.put(Properties.class, lookup.findConstructor(IASProperties.class, MethodType.methodType(void.class, Properties.class)));
            toConcreteMethods.put(List.class, lookup.findStatic(IASStringUtils.class, "convertStringList", MethodType.methodType(List.class, List.class)));
            toConcreteMethods.put(String[].class, lookup.findStatic(IASStringUtils.class, "convertStringArray", MethodType.methodType(IASString[].class, String[].class)));

            toOrigMethods.put(String.class, lookup.findVirtual(IASString.class, "getString", MethodType.methodType(String.class)));
            toOrigMethods.put(StringBuilder.class, lookup.findVirtual(IASStringBuilder.class, "getStringBuilder", MethodType.methodType(StringBuilder.class)));
            toOrigMethods.put(StringBuffer.class, lookup.findVirtual(IASStringBuffer.class, "getStringBuffer", MethodType.methodType(StringBuffer.class)));
            toOrigMethods.put(Formatter.class, lookup.findVirtual(IASFormatter.class, "getFormatter", MethodType.methodType(Formatter.class)));
            toOrigMethods.put(Matcher.class, lookup.findVirtual(IASMatcher.class, "getMatcher", MethodType.methodType(Matcher.class)));
            toOrigMethods.put(Pattern.class, lookup.findVirtual(IASPattern.class, "getPattern", MethodType.methodType(Pattern.class)));
            toOrigMethods.put(Properties.class, lookup.findVirtual(IASProperties.class, "getProperties", MethodType.methodType(Properties.class)));
            toOrigMethods.put(List.class, lookup.findStatic(IASStringUtils.class, "convertTStringList", MethodType.methodType(List.class, List.class)));
            toOrigMethods.put(String[].class, lookup.findStatic(IASStringUtils.class, "convertTaintAwareStringArray", MethodType.methodType(String[].class, IASString[].class)));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Object convertObject(Object object, Map<Class<?>, Function<Object, Object>> converters, Map<Class<?>, Class<?>> classConverters) {
        if (object == null) {
            return null;
        }

        if (object instanceof List) {
            List list = (List) object;
            List result = new ArrayList<>();
            for (Object listEntry : list) {
                Object converted = convertObject(listEntry, converters, classConverters);
                result.add(converted);
            }
            return result;
        }

        if (object instanceof Class) {
            return convertClass((Class<?>) object, classConverters);
        }

        boolean isArray = object.getClass().isArray();
        Class<?> cls = isArray ? object.getClass().getComponentType() : object.getClass();
        for (Class<?> handler : converters.keySet()) {
            if (handler.isAssignableFrom(cls)) {
                if (isArray) {
                    Object[] array = (Object[]) object;
                    Class<?> arrayType = classConverters.get(handler);
                    Object[] result = (Object[]) Array.newInstance(arrayType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        result[i] = convertObject(array[i], converters, classConverters);
                    }
                    return result;
                } else {
                    return converters.get(handler).apply(object);
                }
            }
        }
        return object;
    }

    public static Object convertToOrig(Object object) {
        return convertObject(object, toOrig, toOrigClass);
    }

    private static boolean isHandlable(Class cls) {
        return cls == String.class || cls == StringBuilder.class || cls == StringBuffer.class || cls == Formatter.class || cls == Pattern.class || cls == Matcher.class || cls == Properties.class;
    }

    public static Object convertToConcrete(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof IASTaintAware) {
            return object;
        } else if (object instanceof Class) {
            return object;
        }
        Class cls = object.getClass();
        if (cls == String.class) {
            return IASString.valueOf((String) object);
        } else if (cls == StringBuilder.class) {
            return IASStringBuilder.fromStringBuilder((StringBuilder) object);
        } else if (cls == StringBuffer.class) {
            return IASStringBuffer.fromStringBuffer((StringBuffer) object);
        } else if (cls == Formatter.class) {
            return IASFormatter.fromFormatter((Formatter) object);
        } else if (cls == Matcher.class) {
            return IASMatcher.fromMatcher((Matcher) object);
        } else if (cls == Pattern.class) {
            return IASPattern.fromPattern((Pattern) object);
        } else if (cls == Properties.class) {
            return IASProperties.fromProperties((Properties) object);
        } else if (cls.isArray()) {
            cls = object.getClass().getComponentType();
            if (!cls.isPrimitive() && isHandlable(cls)) {
                Object[] array = (Object[]) object;
                Class<?> arrayType = (Class<?>) convertClassToConcrete(cls);
                Object[] result = (Object[]) Array.newInstance(arrayType, array.length);
                for (int i = 0; i < array.length; i++) {
                    result[i] = convertToConcrete(array[i]);
                }
                return result;
            }
        }
        return object;

        // Unoptimized version:
//        Statistics.INSTANCE.countConversion(object);
//        if (object instanceof IASTaintAware) {
//            return object;
//        }
//        if (object instanceof String) {
//            Statistics.INSTANCE.countConversionUtilsShortcut();
//            return factory.createString((String) object);
//        }
//        Statistics.INSTANCE.countConversionUtilsLong();
//        return convertObject(object, toConcrete);
    }

    public static Class<?> convertClassToOrig(Class<?> cls) {
        return convertClass(cls, toOrigClass);
    }

    public static Class<?> convertClassToConcrete(Class<?> cls) {
        return convertClass(cls, toConcreteClass);
    }

    public static Class<?> convertClass(Class<?> cls, Map<Class<?>, Class<?>> converters) {
        Class<?> baseClass = cls;
        if (cls.isArray()) {
            baseClass = cls.getComponentType();
        }
        Class<?> converted = converters.get(baseClass);

        if (converted == null) {
            return cls;
        }

        if (cls.isArray()) {
            try {
                return Class.forName(cls.getName().replace(baseClass.getName(), converted.getName()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not convert array type " + cls.getName() + " to instrumented type", e);
            }
        }
        return converted;
    }

    public static MethodHandle getToInstrumentedConverter(Class<?> uninstrumentedClass) {
        return toConcreteMethods.get(uninstrumentedClass);
    }

    public static MethodHandle getToOriginalConverter(Class<?> uninstrumentedClass) {
        return toOrigMethods.get(uninstrumentedClass);
    }
}
