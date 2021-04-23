package com.sap.fontus.utils;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversionUtils {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();

    private static final Map<Class<?>, Function<Object, Object>> toConcrete = new HashMap<>();
    private static final Map<Class<?>, Function<Object, Object>> toOrig = new HashMap<>();
    private static final Map<Class<?>, Function<Object, Object>> toInterface = new HashMap<>();

    static {
        toConcrete.put(String.class, (obj) -> {
            if (obj instanceof Class) {
                return factory.getStringClass();
            }
            return factory.createString((String) obj);
        });
        toConcrete.put(StringBuilder.class, (obj) -> factory.createStringBuilder((StringBuilder) obj));
        toConcrete.put(StringBuffer.class, (obj) -> factory.createStringBuffer((StringBuffer) obj));
        toConcrete.put(Formatter.class, (obj) -> factory.createFormatter((Formatter) obj));
        toConcrete.put(Matcher.class, (obj) -> factory.createMatcher((Matcher) obj));
        toConcrete.put(Pattern.class, (obj) -> factory.createPattern((Pattern) obj));
        toConcrete.put(Properties.class, (obj) -> factory.createProperties((Properties) obj));

        toOrig.put(IASStringable.class, (obj) -> {
            if (obj instanceof Class) {
                return String.class;
            }
            return ((IASStringable) obj).getString();
        });
        toOrig.put(IASStringBuilderable.class, (obj) -> {
            if (obj instanceof Class) {
                return StringBuilder.class;
            }
            return ((IASStringBuilderable) obj).getStringBuilder();
        });
        toOrig.put(IASStringBufferable.class, (obj) -> {
            if (obj instanceof Class) {
                return StringBuffer.class;
            }
            return ((IASStringBufferable) obj).getStringBuffer();
        });
        toOrig.put(IASFormatterable.class, (obj) -> {
            if (obj instanceof Class) {
                return Formatter.class;
            }
            return ((IASFormatterable) obj).getFormatter();
        });
        toOrig.put(IASMatcherable.class, (obj) -> {
            if (obj instanceof Class) {
                return Matcher.class;
            }
            return ((IASMatcherable) obj).getMatcher();
        });
        toOrig.put(IASPatternable.class, (obj) -> {
            if (obj instanceof Class) {
                return Pattern.class;
            }
            return ((IASPatternable) obj).getPattern();
        });
        toOrig.put(IASProperties.class, (obj) -> {
            if (obj instanceof Class) {
                return Properties.class;
            }
            return ((IASProperties) obj).getProperties();
        });
    }

    private static Object convertObject(Object object, Map<Class<?>, Function<Object, Object>> converters) {
        if (object == null) {
            return null;
        }

        if (object instanceof List) {
            List list = (List) object;
            List result = new ArrayList<>();
            for (Object listEntry : list) {
                Object converted = convertObject(listEntry, converters);
                result.add(converted);
            }
            return result;
        }

        boolean isArray = object.getClass().isArray();
        Class<?> cls = isArray ? object.getClass().getComponentType() : object.getClass();
        for (Class<?> handler : converters.keySet()) {
            if (handler.isAssignableFrom(cls)) {
                if (isArray) {
                    Object[] array = (Object[]) object;
                    Class<?> arrayType = (Class<?>) converters.get(handler).apply(handler);
                    Object[] result = (Object[]) Array.newInstance(arrayType, array.length);
                    for (int i = 0; i < array.length; i++) {
                        result[i] = convertObject(array[i], converters);
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
        return convertObject(object, toOrig);
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
        return convertClass(cls, toOrig);
    }

    public static Class<?> convertClassToInterface(Class<?> cls) {
        return convertClass(cls, toInterface);
    }

    public static Class<?> convertClassToConcrete(Class<?> cls) {
        return convertClass(cls, toConcrete);
    }

    public static Class<?> convertClass(Class<?> cls, Map<Class<?>, Function<Object, Object>> converters) {
        for (Class<?> handler : converters.keySet()) {
            if (handler.isAssignableFrom(cls)) {
                return (Class<?>) converters.get(handler).apply(cls);
            }
        }
        return cls;
    }
}
