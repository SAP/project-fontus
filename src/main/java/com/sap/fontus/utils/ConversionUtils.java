package com.sap.fontus.utils;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;

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
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();

    private static final Map<Class<?>, Function<Object, Object>> toConcrete = new HashMap<>();
    private static final Map<Class<?>, Function<Object, Object>> toOrig = new HashMap<>();
    private static final Map<Class<?>, Function<Object, Object>> toInterface = new HashMap<>();
    private static final Map<Class<?>, MethodHandle> toOrigMethods = new HashMap<>();
    private static final Map<Class<?>, MethodHandle> toConcreteMethods = new HashMap<>();
    private static final Map<Class<?>, Class<?>> toConcreteClass = new HashMap<>();
    private static final Map<Class<?>, Class<?>> toOrigClass = new HashMap<>();
    private static final Map<Class<?>, Class<?>> toInterfaceClass = new HashMap<>();

    static {
        toConcrete.put(String.class, (obj) -> factory.createString((String) obj));
        toConcrete.put(StringBuilder.class, (obj) -> factory.createStringBuilder((StringBuilder) obj));
        toConcrete.put(StringBuffer.class, (obj) -> factory.createStringBuffer((StringBuffer) obj));
        toConcrete.put(Formatter.class, (obj) -> factory.createFormatter((Formatter) obj));
        toConcrete.put(Matcher.class, (obj) -> factory.createMatcher((Matcher) obj));
        toConcrete.put(Pattern.class, (obj) -> factory.createPattern((Pattern) obj));
        toConcrete.put(Properties.class, (obj) -> factory.createProperties((Properties) obj));

        toOrig.put(IASStringable.class, (obj) -> ((IASStringable) obj).getString());
        toOrig.put(IASStringBuilderable.class, (obj) -> ((IASStringBuilderable) obj).getStringBuilder());
        toOrig.put(IASStringBufferable.class, (obj) -> ((IASStringBufferable) obj).getStringBuffer());
        toOrig.put(IASFormatterable.class, (obj) -> ((IASFormatterable) obj).getFormatter());
        toOrig.put(IASMatcherable.class, (obj) -> ((IASMatcherable) obj).getMatcher());
        toOrig.put(IASPatternable.class, (obj) -> ((IASPatternable) obj).getPattern());
        toOrig.put(IASProperties.class, (obj) -> ((IASProperties) obj).getProperties());

        toInterfaceClass.put(String.class, IASStringable.class);
        toInterfaceClass.put(StringBuilder.class, IASAbstractStringBuilderable.class);
        toInterfaceClass.put(StringBuffer.class, IASAbstractStringBuilderable.class);
        toInterfaceClass.put(Formatter.class, IASFormatterable.class);
        toInterfaceClass.put(Matcher.class, IASMatcherable.class);
        toInterfaceClass.put(Pattern.class, IASPatternable.class);
        toInterfaceClass.put(Properties.class, IASProperties.class);

        toInterfaceClass.put(factory.getStringClass(), IASStringable.class);
        toInterfaceClass.put(factory.getStringBuilderClass(), IASAbstractStringBuilderable.class);
        toInterfaceClass.put(factory.getStringBufferClass(), IASAbstractStringBuilderable.class);
        toInterfaceClass.put(factory.getFormatterClass(), IASFormatterable.class);
        toInterfaceClass.put(factory.getMatcherClass(), IASMatcherable.class);
        toInterfaceClass.put(factory.getPatternClass(), IASPatternable.class);
        toInterfaceClass.put(factory.getPropertiesClass(), IASProperties.class);

        toOrigClass.put(IASStringable.class, String.class);
        toOrigClass.put(IASStringBuilderable.class, StringBuilder.class);
        toOrigClass.put(IASStringBufferable.class, StringBuffer.class);
        toOrigClass.put(IASFormatterable.class, Formatter.class);
        toOrigClass.put(IASMatcherable.class, Matcher.class);
        toOrigClass.put(IASPatternable.class, Pattern.class);
        toOrigClass.put(IASProperties.class, Properties.class);

        toOrigClass.put(factory.getStringClass(), String.class);
        toOrigClass.put(factory.getStringBuilderClass(), StringBuilder.class);
        toOrigClass.put(factory.getStringBufferClass(), StringBuffer.class);
        toOrigClass.put(factory.getFormatterClass(), Formatter.class);
        toOrigClass.put(factory.getMatcherClass(), Matcher.class);
        toOrigClass.put(factory.getPatternClass(), Pattern.class);
        toOrigClass.put(factory.getPropertiesClass(), Properties.class);

        toConcreteClass.put(String.class, factory.getStringClass());
        toConcreteClass.put(StringBuilder.class, factory.getStringBuilderClass());
        toConcreteClass.put(StringBuffer.class, factory.getStringBufferClass());
        toConcreteClass.put(Formatter.class, factory.getFormatterClass());
        toConcreteClass.put(Matcher.class, factory.getMatcherClass());
        toConcreteClass.put(Pattern.class, factory.getPatternClass());
        toConcreteClass.put(Properties.class, factory.getPropertiesClass());

        TaintStringConfig stringConfig = Configuration.getConfiguration().getTaintStringConfig();
        try {
            toConcreteMethods.put(String.class, lookup.findConstructor(Class.forName(Utils.slashToDot(stringConfig.getTStringQN())), MethodType.methodType(void.class, String.class)));
            toConcreteMethods.put(StringBuilder.class, lookup.findConstructor(Class.forName(Utils.slashToDot(stringConfig.getTStringBuilderQN())), MethodType.methodType(void.class, StringBuilder.class)));
            toConcreteMethods.put(StringBuffer.class, lookup.findConstructor(Class.forName(Utils.slashToDot(stringConfig.getTStringBufferQN())), MethodType.methodType(void.class, StringBuffer.class)));
            toConcreteMethods.put(Formatter.class, lookup.findConstructor(Class.forName(Utils.slashToDot(stringConfig.getTFormatterQN())), MethodType.methodType(void.class, Formatter.class)));
            toConcreteMethods.put(Matcher.class, lookup.findConstructor(Class.forName(Utils.slashToDot(stringConfig.getTMatcherQN())), MethodType.methodType(void.class, Matcher.class)));
            toConcreteMethods.put(Pattern.class, lookup.findConstructor(Class.forName(Utils.slashToDot(stringConfig.getTPatternQN())), MethodType.methodType(void.class, Pattern.class)));
            toConcreteMethods.put(Properties.class, lookup.findConstructor(Class.forName(Utils.slashToDot(stringConfig.getTPropertiesQN())), MethodType.methodType(void.class, Properties.class)));
            toConcreteMethods.put(List.class, lookup.findStatic(IASStringUtils.class, "convertStringList", MethodType.methodType(List.class, List.class)));
            toConcreteMethods.put(String[].class, lookup.findStatic(IASStringUtils.class, "convertStringArray", MethodType.methodType(IASStringable[].class, String[].class)));

            toOrigMethods.put(String.class, lookup.findVirtual(IASStringable.class, "getString", MethodType.methodType(String.class)));
            toOrigMethods.put(StringBuilder.class, lookup.findVirtual(IASStringBuilderable.class, "getStringBuilder", MethodType.methodType(StringBuilder.class)));
            toOrigMethods.put(StringBuffer.class, lookup.findVirtual(IASStringBufferable.class, "getStringBuffer", MethodType.methodType(StringBuffer.class)));
            toOrigMethods.put(Formatter.class, lookup.findVirtual(IASFormatterable.class, "getFormatter", MethodType.methodType(Formatter.class)));
            toOrigMethods.put(Matcher.class, lookup.findVirtual(IASMatcherable.class, "getMatcher", MethodType.methodType(Matcher.class)));
            toOrigMethods.put(Pattern.class, lookup.findVirtual(IASPatternable.class, "getPattern", MethodType.methodType(Pattern.class)));
            toOrigMethods.put(Properties.class, lookup.findVirtual(IASProperties.class, "getProperties", MethodType.methodType(Properties.class)));
            toOrigMethods.put(List.class, lookup.findStatic(IASStringUtils.class, "convertTStringList", MethodType.methodType(List.class, List.class)));
            toOrigMethods.put(String[].class, lookup.findStatic(IASStringUtils.class, "convertTaintAwareStringArray", MethodType.methodType(String[].class, IASStringable[].class)));
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
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
            return factory.createString((String) object);
        } else if (cls == StringBuilder.class) {
            return factory.createStringBuilder((StringBuilder) object);
        } else if (cls == StringBuffer.class) {
            return factory.createStringBuffer((StringBuffer) object);
        } else if (cls == Formatter.class) {
            return factory.createFormatter((Formatter) object);
        } else if (cls == Matcher.class) {
            return factory.createMatcher((Matcher) object);
        } else if (cls == Pattern.class) {
            return factory.createPattern((Pattern) object);
        } else if (cls == Properties.class) {
            return factory.createProperties((Properties) object);
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

    public static Class<?> convertClassToInterface(Class<?> cls) {
        return convertClass(cls, toInterfaceClass);
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
