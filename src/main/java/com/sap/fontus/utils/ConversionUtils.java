package com.sap.fontus.utils;

import com.sap.fontus.taintaware.unified.*;
import com.sap.fontus.taintaware.unified.reflect.*;
import com.sap.fontus.taintaware.unified.reflect.type.IASTypeVariableImpl;
import com.sap.fontus.taintaware.unified.reflect.type.IASWildcardTypeImpl;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.ObjectInputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversionUtils {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final CombinedExcludedLookup excludedLookup = new CombinedExcludedLookup();

    private static final List<Converter> instrumenter = Arrays.asList(
            new NullConverter(),
            new AlreadyTaintedConverter(),
            new ClassConverter((cls) -> cls),
            new TypeConverter(ConversionUtils::convertTypeToInstrumented),
            new DefaultConverter<>(String.class, IASString::valueOf),
            new DefaultConverter<>(StringBuilder.class, IASStringBuilder::fromStringBuilder),
            new DefaultConverter<>(StringBuffer.class, IASStringBuffer::fromStringBuffer),
            new DefaultConverter<>(Formatter.class, IASFormatter::fromFormatter),
            new DefaultConverter<>(Matcher.class, IASMatcher::fromMatcher),
            new DefaultConverter<>(Pattern.class, IASPattern::fromPattern),
            new DefaultConverter<>(Properties.class, IASProperties::fromProperties),
            new DefaultConverter<>(Method.class, IASReflectRegistry.getInstance()::map),
            new DefaultConverter<>(AccessibleObject.class, IASReflectRegistry.getInstance()::mapAccessibleObject),
            new DefaultConverter<>(Executable.class, IASReflectRegistry.getInstance()::mapExecutable),
            new DefaultConverter<>(Field.class, IASReflectRegistry.getInstance()::map),
            new DefaultConverter<>(Member.class, IASReflectRegistry.getInstance()::mapMember),
            new DefaultConverter<>(Constructor.class, IASReflectRegistry.getInstance()::map),
            new DefaultConverter<>(Parameter.class, IASParameter::new),
            new ArrayConverter(ConversionUtils::convertToInstrumented, ConversionUtils::convertClassToConcrete),
            new ListConverter(ConversionUtils::convertToInstrumented),
            new SetConverter(ConversionUtils::convertToInstrumented)
    );

    private static final List<Converter> uninstrumenter = Arrays.asList(
            new NullConverter(),
            new ClassConverter(ConversionUtils::convertClassToOrig),
            new TypeConverter(ConversionUtils::convertTypeToUninstrumented),
            new DefaultConverter<>(IASString.class, IASString::toString),
            new DefaultConverter<>(IASStringBuilder.class, IASStringBuilder::getStringBuilder),
            new DefaultConverter<>(IASStringBuffer.class, IASStringBuffer::getStringBuffer),
            new DefaultConverter<>(IASFormatter.class, IASFormatter::getFormatter),
            new DefaultConverter<>(IASMatcher.class, IASMatcher::getMatcher),
            new DefaultConverter<>(IASPattern.class, IASPattern::getPattern),
            new DefaultConverter<>(IASProperties.class, IASProperties::getProperties),
            new DefaultConverter<>(IASMethod.class, IASMethod::getMethod),
            new DefaultConverter<>(IASField.class, IASField::getField),
            new DefaultConverter<>(IASConstructor.class, IASConstructor::getConstructor),
            new DefaultConverter<>(IASMember.class, IASMember::getMember),
            new DefaultConverter<>(IASExecutable.class, IASExecutable::getExecutable),
            new DefaultConverter<>(IASAccessibleObject.class, IASAccessibleObject::getAccessibleObject),
            new DefaultConverter<>(IASParameter.class, IASParameter::getParameter),
            new ArrayConverter(ConversionUtils::convertToUninstrumented, ConversionUtils::convertClassToOrig),
            new ListConverter(ConversionUtils::convertToUninstrumented),
            new SetConverter(ConversionUtils::convertToUninstrumented),
            new AlreadyUntaintedConverter()
    );

    public static Object convertToInstrumented(Object object) {
        for (Converter converter : instrumenter) {
            if (converter.canConvert(object)) {
                return converter.convert(object);
            }
        }
        return object;
    }

    private static final Map<Class<?>, MethodHandle> toOrigMethods = new HashMap<>();
    private static final Map<Class<?>, MethodHandle> toConcreteMethods = new HashMap<>();
    private static final Map<Class<?>, Class<?>> toConcreteClass = new HashMap<>();

    private static final Map<Class<?>, Class<?>> toOrigClass = new HashMap<>();

    static {
        toOrigClass.put(IASString.class, String.class);
        toOrigClass.put(IASStringBuilder.class, StringBuilder.class);
        toOrigClass.put(IASStringBuffer.class, StringBuffer.class);
        toOrigClass.put(IASFormatter.class, Formatter.class);
        toOrigClass.put(IASMatcher.class, Matcher.class);
        toOrigClass.put(IASPattern.class, Pattern.class);
        toOrigClass.put(IASProperties.class, Properties.class);
        toOrigClass.put(IASAccessibleObject.class, AccessibleObject.class);
        toOrigClass.put(IASExecutable.class, Executable.class);
        toOrigClass.put(IASConstructor.class, Constructor.class);
        toOrigClass.put(IASField.class, Field.class);
        toOrigClass.put(IASMember.class, Member.class);
        toOrigClass.put(IASMethod.class, Method.class);
        toOrigClass.put(IASParameter.class, Parameter.class);
        toOrigClass.put(IASObjectInputStream.class, ObjectInputStream.class);

        toConcreteClass.put(String.class, IASString.class);
        toConcreteClass.put(StringBuilder.class, IASStringBuilder.class);
        toConcreteClass.put(StringBuffer.class, IASStringBuffer.class);
        toConcreteClass.put(Formatter.class, IASFormatter.class);
        toConcreteClass.put(Matcher.class, IASMatcher.class);
        toConcreteClass.put(Pattern.class, IASPattern.class);
        toConcreteClass.put(AccessibleObject.class, IASAccessibleObject.class);
        toConcreteClass.put(Executable.class, IASExecutable.class);
        toConcreteClass.put(Field.class, IASField.class);
        toConcreteClass.put(Member.class, IASMember.class);
        toConcreteClass.put(Method.class, IASMethod.class);
        toConcreteClass.put(Parameter.class, IASParameter.class);
        toConcreteClass.put(Constructor.class, IASConstructor.class);

        try {
            toConcreteMethods.put(String.class, lookup.findConstructor(IASString.class, MethodType.methodType(void.class, String.class)));
            toConcreteMethods.put(StringBuilder.class, lookup.findConstructor(IASStringBuilder.class, MethodType.methodType(void.class, StringBuilder.class)));
            toConcreteMethods.put(StringBuffer.class, lookup.findConstructor(IASStringBuffer.class, MethodType.methodType(void.class, StringBuffer.class)));
            toConcreteMethods.put(Formatter.class, lookup.findConstructor(IASFormatter.class, MethodType.methodType(void.class, Formatter.class)));
            toConcreteMethods.put(Matcher.class, lookup.findConstructor(IASMatcher.class, MethodType.methodType(void.class, Matcher.class)));
            toConcreteMethods.put(Pattern.class, lookup.findConstructor(IASPattern.class, MethodType.methodType(void.class, Pattern.class)));
            toConcreteMethods.put(Properties.class, lookup.findConstructor(IASProperties.class, MethodType.methodType(void.class, Properties.class)));
            toConcreteMethods.put(AccessibleObject.class, lookup.findVirtual(IASReflectRegistry.class, "mapAccessibleObject", MethodType.methodType(IASAccessibleObject.class, AccessibleObject.class)).bindTo(IASReflectRegistry.getInstance()));
            toConcreteMethods.put(Executable.class, lookup.findVirtual(IASReflectRegistry.class, "mapExecutable", MethodType.methodType(IASExecutable.class, Executable.class)).bindTo(IASReflectRegistry.getInstance()));
            toConcreteMethods.put(Constructor.class, lookup.findVirtual(IASReflectRegistry.class, "map", MethodType.methodType(IASConstructor.class, Constructor.class)).bindTo(IASReflectRegistry.getInstance()));
            toConcreteMethods.put(Method.class, lookup.findVirtual(IASReflectRegistry.class, "map", MethodType.methodType(IASMethod.class, Method.class)).bindTo(IASReflectRegistry.getInstance()));
            toConcreteMethods.put(Field.class, lookup.findVirtual(IASReflectRegistry.class, "map", MethodType.methodType(IASField.class, Field.class)).bindTo(IASReflectRegistry.getInstance()));
            toConcreteMethods.put(Member.class, lookup.findVirtual(IASReflectRegistry.class, "mapMember", MethodType.methodType(IASMember.class, Member.class)).bindTo(IASReflectRegistry.getInstance()));
            toConcreteMethods.put(Parameter.class, lookup.findConstructor(IASParameter.class, MethodType.methodType(void.class, Parameter.class)));
            toConcreteMethods.put(List.class, lookup.findStatic(IASStringUtils.class, "convertStringList", MethodType.methodType(List.class, List.class)));
            toConcreteMethods.put(String[].class, lookup.findStatic(IASStringUtils.class, "convertStringArray", MethodType.methodType(IASString[].class, String[].class)));

            toOrigMethods.put(String.class, lookup.findVirtual(IASString.class, "getString", MethodType.methodType(String.class)));
            toOrigMethods.put(StringBuilder.class, lookup.findVirtual(IASStringBuilder.class, "getStringBuilder", MethodType.methodType(StringBuilder.class)));
            toOrigMethods.put(StringBuffer.class, lookup.findVirtual(IASStringBuffer.class, "getStringBuffer", MethodType.methodType(StringBuffer.class)));
            toOrigMethods.put(Formatter.class, lookup.findVirtual(IASFormatter.class, "getFormatter", MethodType.methodType(Formatter.class)));
            toOrigMethods.put(Matcher.class, lookup.findVirtual(IASMatcher.class, "getMatcher", MethodType.methodType(Matcher.class)));
            toOrigMethods.put(Pattern.class, lookup.findVirtual(IASPattern.class, "getPattern", MethodType.methodType(Pattern.class)));
            toOrigMethods.put(Properties.class, lookup.findVirtual(IASProperties.class, "getProperties", MethodType.methodType(Properties.class)));
            toOrigMethods.put(AccessibleObject.class, lookup.findVirtual(IASAccessibleObject.class, "getAccessibleObject", MethodType.methodType(AccessibleObject.class)));
            toOrigMethods.put(Executable.class, lookup.findVirtual(IASExecutable.class, "getExecutable", MethodType.methodType(Executable.class)));
            toOrigMethods.put(Constructor.class, lookup.findVirtual(IASConstructor.class, "getConstructor", MethodType.methodType(Constructor.class)));
            toOrigMethods.put(Member.class, lookup.findVirtual(IASMember.class, "getMember", MethodType.methodType(Member.class)));
            toOrigMethods.put(Method.class, lookup.findVirtual(IASMethod.class, "getMethod", MethodType.methodType(Method.class)));
            toOrigMethods.put(Parameter.class, lookup.findVirtual(IASParameter.class, "getParameter", MethodType.methodType(Parameter.class)));
            toOrigMethods.put(Field.class, lookup.findVirtual(IASField.class, "getField", MethodType.methodType(Field.class)));
            toOrigMethods.put(List.class, lookup.findStatic(IASStringUtils.class, "convertTStringList", MethodType.methodType(List.class, List.class)));
            toOrigMethods.put(String[].class, lookup.findStatic(IASStringUtils.class, "convertTaintAwareStringArray", MethodType.methodType(String[].class, IASString[].class)));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object convertToUninstrumented(Object object) {
        for (Converter converter : uninstrumenter) {
            if (converter.canConvert(object)) {
                return converter.convert(object);
            }
        }
        return object;
    }

    // TODO: Can't we get the list of the classes based on the Maps above? This is super ugly and error prone (it seems to be missing entries, but that might be what we want?
    private static boolean isHandlable(Class cls) {
        return cls == String.class || cls == StringBuilder.class || cls == StringBuffer.class || cls == Formatter.class || cls == Pattern.class || cls == Matcher.class || cls == Properties.class || Type.class.isAssignableFrom(cls) || cls == Method.class || cls == Field.class;
    }

    public static Class<?> convertClassToOrig(Class<?> cls) {
        return convertClass(cls, toOrigClass);
    }

    public static Class<?> convertClassToConcrete(Class<?> cls) {
        return convertClass(cls, toConcreteClass);
    }

    private static Class<?> convertClass(Class<?> cls, Map<Class<?>, Class<?>> converters) {
        Class<?> baseClass = cls;
        int dimension = 0;
        while (baseClass.isArray()) {
            dimension += 1;
            baseClass = baseClass.getComponentType();
        }

        Class<?> converted = converters.get(baseClass);

        if (converted == null) {
            return cls;
        }

        if (cls.isArray()) {
            return Array.newInstance(converted, new int[dimension]).getClass();
        }
        return converted;
    }

    public static MethodHandle getToInstrumentedConverter(Class<?> uninstrumentedClass) {
        return toConcreteMethods.get(uninstrumentedClass);
    }

    public static MethodHandle getToOriginalConverter(Class<?> uninstrumentedClass) {
        return toOrigMethods.get(uninstrumentedClass);
    }

    public static Type convertTypeToUninstrumented(Type type) {
        if (type instanceof Class) {
            return convertClassToOrig((Class<?>) type);
        } else if (type instanceof GenericArrayType) {
            return GenericArrayTypeImpl.make(convertTypeToUninstrumented(((GenericArrayType) type).getGenericComponentType()));
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            return ParameterizedTypeImpl.make(
                    (Class<?>) convertTypeToUninstrumented(pType.getRawType()),
                    Arrays.stream(pType.getActualTypeArguments()).map(ConversionUtils::convertTypeToUninstrumented).toArray(Type[]::new),
                    convertTypeToUninstrumented(pType.getOwnerType())
            );
        } else if (type instanceof IASTypeVariableImpl) {
            return ((IASTypeVariableImpl<?>) type).getType();
        } else if (type instanceof IASWildcardTypeImpl) {
            return ((IASWildcardTypeImpl) type).getType();
        }
        return type;
    }

    public static <T extends GenericDeclaration> Type convertTypeToInstrumented(Type type) {
        if (type == null) {
            return null;
        } else if (excludedLookup.isFontusClass(type.getClass())) {
            return type;
        } else if (type instanceof Class) {
            return convertClassToConcrete((Class<?>) type);
        } else if (type instanceof GenericArrayType) {
            return GenericArrayTypeImpl.make(convertTypeToInstrumented(((GenericArrayType) type).getGenericComponentType()));
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            return ParameterizedTypeImpl.make(
                    (Class<?>) convertTypeToInstrumented(pType.getRawType()),
                    Arrays.stream(pType.getActualTypeArguments()).map(ConversionUtils::convertTypeToInstrumented).toArray(Type[]::new),
                    convertTypeToInstrumented(pType.getOwnerType())
            );
        } else if (type instanceof TypeVariable) {
            return new IASTypeVariableImpl<>((TypeVariable<T>) type);
        } else if (type instanceof WildcardType) {
            return new IASWildcardTypeImpl((WildcardType) type);
        }
        return type;
    }

    private interface Converter {
        boolean canConvert(Object o);

        Object convert(Object o);
    }

    private static class NullConverter implements Converter {
        @Override
        public boolean canConvert(Object o) {
            return o == null;
        }

        @Override
        public Object convert(Object o) {
            return null;
        }
    }

    private static class AlreadyUntaintedConverter implements Converter {

        @Override
        public boolean canConvert(Object o) {
            return o != null && excludedLookup.isPackageExcludedOrJdk(o.getClass());
        }

        @Override
        public Object convert(Object o) {
            return o;
        }
    }

    private static class AlreadyTaintedConverter implements Converter {
        @Override
        public boolean canConvert(Object o) {
            if (o == null) {
                return false;
            }
            return excludedLookup.isFontusClass(o.getClass());
        }

        @Override
        public Object convert(Object o) {
            return o;
        }
    }

    private static class TypeConverter implements Converter {
        private final Function<Type, Type> atomicConverter;

        private TypeConverter(Function<Type, Type> atomicConverter) {
            this.atomicConverter = atomicConverter;
        }

        @Override
        public boolean canConvert(Object o) {
            return o instanceof Type && !excludedLookup.isFontusClass(o.getClass());
        }

        @Override
        public Object convert(Object o) {
            return atomicConverter.apply((Type) o);
        }
    }

    private static class ClassConverter implements Converter {
        private final Function<Class, Class> atomicConverter;

        private ClassConverter(Function<Class, Class> atomicConverter) {
            this.atomicConverter = atomicConverter;
        }


        @Override
        public boolean canConvert(Object o) {
            return o instanceof Class;
        }

        @Override
        public Object convert(Object o) {
            return this.atomicConverter.apply((Class) o);
        }
    }

    private static class ArrayConverter implements Converter {
        private final Function<Object, Object> atomicConverter;
        private final Function<Class, Class> classConverter;

        private ArrayConverter(Function<Object, Object> atomicConverter, Function<Class, Class> classConverter) {
            this.atomicConverter = atomicConverter;
            this.classConverter = classConverter;
        }

        @Override
        public boolean canConvert(Object o) {
            return o != null && o.getClass().isArray();
        }

        @Override
        public Object convert(Object o) {
            Class<?> cls = o.getClass().getComponentType();
            if (!cls.isPrimitive() && isHandlable(convertClassToOrig(cls))) {
                Object[] array = (Object[]) o;
                Class<?> arrayType = classConverter.apply(cls);
                Object[] result = (Object[]) Array.newInstance(arrayType, array.length);
                for (int i = 0; i < array.length; i++) {
                    result[i] = atomicConverter.apply(array[i]);
                }
                return result;
            }
            return o;
        }
    }

    private static class DefaultConverter<T, R> implements Converter {
        private final Class<T> convertable;
        private final Function<T, R> converter;

        public DefaultConverter(Class<T> convertable, Function<T, R> converter) {
            this.convertable = convertable;
            this.converter = converter;
        }

        @Override
        public boolean canConvert(Object o) {
            if (o == null) {
                return false;
            }
            return this.convertable.isAssignableFrom(o.getClass());
        }

        @Override
        public Object convert(Object o) {
            return this.converter.apply((T) o);
        }
    }

    private static class SetConverter implements Converter {
        private final Function<Object, Object> atomicConverter;

        private SetConverter(Function<Object, Object> atomicConverter) {
            this.atomicConverter = atomicConverter;
        }

        @Override
        public boolean canConvert(Object o) {
            // TODO: evil hack to prevent infinite recursion for hibernate collection classes
            return o instanceof Set && !o.getClass().getPackage().getName().equals("org.hibernate.collection.internal");
        }

        @Override
        public Object convert(Object o) {
            if (o instanceof Set) {
                Set<Object> set = (Set<Object>) o;
                Set<Object> result = new HashSet<>();
                boolean changed = false;
                for (Object entry : set) {
                    Object converted = this.atomicConverter.apply(entry);
                    result.add(converted);
                    if (!Objects.equals(entry, converted)) {
                        changed = true;
                    }

                }
                if (!changed) {
                    return set;
                }

                if (set.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                    result = Collections.unmodifiableSet(result);
                }
                return result;
            }
            return null;
        }

    }

    private static class ListConverter implements Converter {
        private final Function<Object, Object> atomicConverter;

        private ListConverter(Function<Object, Object> atomicConverter) {
            this.atomicConverter = atomicConverter;
        }

        @Override
        public boolean canConvert(Object o) {
            // TODO: evil hack to prevent infinite recursion for hibernate collection classes
            return o instanceof List && !o.getClass().getPackage().getName().equals("org.hibernate.collection.internal");
        }

        @Override
        public Object convert(Object o) {
            if (o instanceof List) {
                List list = (List) o;
                List result = new ArrayList();

                for (Object listEntry : list) {
                    Object converted = atomicConverter.apply(listEntry);
                    result.add(converted);
                }

                boolean hasChanged = false;
                for (int i = 0; i < list.size(); i++) {
                    if (!Objects.equals(list.get(i), result.get(i))) {
                        hasChanged = true;
                        break;
                    }
                }

                if (!hasChanged) {
                    return list;
                }

                if (list.getClass().getName().startsWith("java.util.Collections$Unmodifiable")) {
                    result = Collections.unmodifiableList(result);
                } else if (list.getClass().getName().startsWith("java.util.Collections$Singleton")) {
                    result = Collections.singletonList(result);
                }
//            } else if (list.getClass().getName().startsWith("java.util.Collections$Synchronized")){
//                result = Collections.synchronizedList(result);
//            }
//            else if (list.getClass().getName().startsWith("java.util.Collections$Checked")){
//                result = Collections.checkedList(result, );
//            }
                else {
                    for (int i = 0; i < list.size(); i++) {
                        Object converted = result.get(i);
                        list.set(i, converted);
                        result = list;
                    }
                }
                return result;
            }
            return null;
        }
    }
}
