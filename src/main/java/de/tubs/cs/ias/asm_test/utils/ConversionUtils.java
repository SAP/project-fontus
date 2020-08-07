package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.taintaware.shared.*;

import java.lang.reflect.Array;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversionUtils {
    private static final IASFactory factory = Configuration.getConfiguration().getTaintMethod().getFactory();

    private static final Map<Class, Function> toConcrete = new HashMap<>();
    private static final Map<Class, Function> toOrig = new HashMap<>();
    private static final Map<Class, Function> toInterface = new HashMap<>();

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

        toInterface.put(String.class, (obj) -> {
            if (obj instanceof Class) {
                return IASStringable.class;
            }
            return factory.createString((String) obj);
        });
        toInterface.put(StringBuilder.class, (obj) -> factory.createStringBuilder((StringBuilder) obj));
        toInterface.put(StringBuffer.class, (obj) -> factory.createStringBuffer((StringBuffer) obj));
        toInterface.put(Formatter.class, (obj) -> factory.createFormatter((Formatter) obj));
        toInterface.put(Matcher.class, (obj) -> factory.createMatcher((Matcher) obj));
        toInterface.put(Pattern.class, (obj) -> factory.createPattern((Pattern) obj));
        toInterface.put(Properties.class, (obj) -> factory.createProperties((Properties) obj));

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

    private static Object convertObject(Object object, Map<Class, Function> converters) {
        if (object == null) {
            return null;
        }
        boolean isArray = object.getClass().isArray();
        Class cls = isArray ? object.getClass().getComponentType() : object.getClass();
        for (Class handler : converters.keySet()) {
            if (handler.isAssignableFrom(cls)) {
                if (isArray) {
                    Object[] array = (Object[]) object;
                    Class arrayType = (Class) converters.get(handler).apply(handler);
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

    public static Object convertToInterface(Object object) {
        return convertObject(object, toInterface);
    }

    public static Object convertToConcrete(Object object) {
        return convertObject(object, toConcrete);
    }

    public static Object convertClassToOrig(Class cls) {
        return convertClass(cls, toOrig);
    }

    public static Object convertClassToInterface(Class cls) {
        return convertClass(cls, toInterface);
    }

    public static Object convertClassToConcrete(Class cls) {
        return convertClass(cls, toConcrete);
    }

    public static Class convertClass(Class cls, Map<Class, Function> converters) {
        for (Class handler : converters.keySet()) {
            if (handler.isAssignableFrom(cls)) {
                return (Class) converters.get(handler).apply(cls);
            }
        }
        return cls;
    }
}
