package com.sap.fontus.taintaware.unified.runtime;


import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringBuilder;
import com.sap.fontus.taintaware.unified.IASStringUtils;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ObjectMethods {
    private static final MethodType DESCRIPTOR_MT = MethodType.methodType(MethodType.class);
    private static final MethodType NAMES_MT = MethodType.methodType(List.class);
    private static final MethodHandle FALSE;
    private static final MethodHandle TRUE;
    private static final MethodHandle ZERO;
    private static final MethodHandle CLASS_IS_INSTANCE;
    private static final MethodHandle OBJECT_EQUALS;
    private static final MethodHandle OBJECTS_EQUALS;
    private static final MethodHandle OBJECTS_HASHCODE;
    private static final MethodHandle OBJECTS_TOSTRING;
    private static final MethodHandle OBJECTS_TOTSTRING;
    private static final MethodHandle OBJECT_EQ;
    private static final MethodHandle OBJECT_HASHCODE;
    private static final MethodHandle OBJECT_TO_STRING;
    private static final MethodHandle STRING_FORMAT;
    private static final MethodHandle TSTRING_FORMAT;
    private static final MethodHandle HASH_COMBINER;
    private static final HashMap<Class<?>, MethodHandle> primitiveEquals;
    private static final HashMap<Class<?>, MethodHandle> primitiveHashers;
    private static final HashMap<Class<?>, MethodHandle> primitiveToString;
    private static final HashMap<Class<?>, MethodHandle> primitiveToTString;

    private ObjectMethods() {
    }

    private static int hashCombiner(int x, int y) {
        return x * 31 + y;
    }

    private static boolean eq(Object a, Object b) {
        return a == b;
    }

    private static boolean eq(byte a, byte b) {
        return a == b;
    }

    private static boolean eq(short a, short b) {
        return a == b;
    }

    private static boolean eq(char a, char b) {
        return a == b;
    }

    private static boolean eq(int a, int b) {
        return a == b;
    }

    private static boolean eq(long a, long b) {
        return a == b;
    }

    private static boolean eq(float a, float b) {
        return Float.compare(a, b) == 0;
    }

    private static boolean eq(double a, double b) {
        return Double.compare(a, b) == 0;
    }

    private static boolean eq(boolean a, boolean b) {
        return a == b;
    }

    private static MethodHandle equalator(Class<?> clazz) {
        return clazz.isPrimitive() ? primitiveEquals.get(clazz) : OBJECTS_EQUALS.asType(MethodType.methodType(Boolean.TYPE, clazz, clazz));
    }

    private static MethodHandle hasher(Class<?> clazz) {
        return clazz.isPrimitive() ? primitiveHashers.get(clazz) : OBJECTS_HASHCODE.asType(MethodType.methodType(Integer.TYPE, clazz));
    }

    private static MethodHandle stringifier(Class<?> clazz) {
        return clazz.isPrimitive() ? primitiveToString.get(clazz) : OBJECTS_TOSTRING.asType(MethodType.methodType(String.class, clazz));
    }

    private static MethodHandle tstringifier(Class<?> clazz) {
        return clazz.isPrimitive() ? primitiveToTString.get(clazz) : OBJECTS_TOTSTRING.asType(MethodType.methodType(IASString.class, clazz));
    }

    private static MethodHandle makeEquals(Class<?> receiverClass, List<MethodHandle> getters) {
        MethodType rr = MethodType.methodType(Boolean.TYPE, receiverClass, receiverClass);
        MethodType ro = MethodType.methodType(Boolean.TYPE, receiverClass, Object.class);
        MethodHandle instanceFalse = MethodHandles.dropArguments(FALSE, 0, receiverClass, Object.class);
        MethodHandle instanceTrue = MethodHandles.dropArguments(TRUE, 0, receiverClass, Object.class);
        MethodHandle isSameObject = OBJECT_EQ.asType(ro);
        MethodHandle isInstance = MethodHandles.dropArguments(CLASS_IS_INSTANCE.bindTo(receiverClass), 0, receiverClass);
        MethodHandle accumulator = MethodHandles.dropArguments(TRUE, 0, receiverClass, receiverClass);

        MethodHandle thisFieldEqual;
        for(Iterator<MethodHandle> it = getters.iterator(); it.hasNext(); accumulator = MethodHandles.guardWithTest(thisFieldEqual, accumulator, instanceFalse.asType(rr))) {
            MethodHandle getter = it.next();
            MethodHandle equalator = equalator(getter.type().returnType());
            thisFieldEqual = MethodHandles.filterArguments(equalator, 0, getter, getter);
        }

        return MethodHandles.guardWithTest(isSameObject, instanceTrue, MethodHandles.guardWithTest(isInstance, accumulator.asType(ro), instanceFalse));
    }

    private static MethodHandle makeHashCode(Class<?> receiverClass, List<MethodHandle> getters) {
        MethodHandle accumulator = MethodHandles.dropArguments(ZERO, 0, receiverClass);

        MethodHandle combineHashes;
        for(Iterator<MethodHandle> it = getters.iterator(); it.hasNext(); accumulator = MethodHandles.permuteArguments(combineHashes, accumulator.type(), 0, 0)) {
            MethodHandle getter = it.next();
            MethodHandle hasher = hasher(getter.type().returnType());
            MethodHandle hashThisField = MethodHandles.filterArguments(hasher, 0, getter);
            combineHashes = MethodHandles.filterArguments(HASH_COMBINER, 0, accumulator, hashThisField);
        }

        return accumulator;
    }

    private static MethodHandle makeToString(Class<?> receiverClass, List<MethodHandle> getters, List<String> names) {
        assert getters.size() == names.size();

        int[] invArgs = new int[getters.size()];
        Arrays.fill(invArgs, 0);
        MethodHandle[] filters = new MethodHandle[getters.size()];
        StringBuilder sb = new StringBuilder();
        sb.append(receiverClass.getSimpleName()).append("[");

        MethodHandle formatter;
        MethodHandle filtered;
        for(int i = 0; i < getters.size(); ++i) {
            formatter = getters.get(i);
            filtered = stringifier(formatter.type().returnType());
            MethodHandle stringifyThisField = MethodHandles.filterArguments(filtered, 0, formatter);
            filters[i] = stringifyThisField;
            sb.append(names.get(i)).append("=%s");
            if (i != getters.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(']');
        String formatString = sb.toString();
        formatter = MethodHandles.insertArguments(STRING_FORMAT, 0, formatString).asCollector(String[].class, getters.size());
        if (getters.isEmpty()) {
            formatter = MethodHandles.dropArguments(formatter, 0, receiverClass);
        } else {
            filtered = MethodHandles.filterArguments(formatter, 0, filters);
            formatter = MethodHandles.permuteArguments(filtered, MethodType.methodType(String.class, receiverClass), invArgs);
        }

        return formatter;
    }

    private static MethodHandle makeToTString(Class<?> receiverClass, List<MethodHandle> getters, List<String> names) {
        assert getters.size() == names.size();

        int[] invArgs = new int[getters.size()];
        Arrays.fill(invArgs, 0);
        MethodHandle[] filters = new MethodHandle[getters.size()];
        IASStringBuilder sb = new IASStringBuilder();
        sb.append(receiverClass.getSimpleName()).append("[");

        MethodHandle formatter;
        MethodHandle filtered;
        for(int i = 0; i < getters.size(); ++i) {
            formatter = getters.get(i);
            filtered = tstringifier(formatter.type().returnType());
            MethodHandle stringifyThisField = MethodHandles.filterArguments(filtered, 0, formatter);
            filters[i] = stringifyThisField;
            sb.append(names.get(i)).append("=%s");
            if (i != getters.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(']');
        IASString formatString = sb.toIASString();
        formatter = MethodHandles.insertArguments(TSTRING_FORMAT, 0, formatString).asCollector(IASString[].class, getters.size());
        if (getters.isEmpty()) {
            formatter = MethodHandles.dropArguments(formatter, 0, receiverClass);
        } else {
            filtered = MethodHandles.filterArguments(formatter, 0, filters);
            formatter = MethodHandles.permuteArguments(filtered, MethodType.methodType(IASString.class, receiverClass), invArgs);
        }

        return formatter;
    }

    public static Object bootstrap(MethodHandles.Lookup lookup, String methodName, TypeDescriptor type, Class<?> recordClass, String names, MethodHandle... getters) throws Throwable {
        MethodType methodType = null;
        if (type instanceof MethodType) {
            methodType = (MethodType) type;
        } else {
            if (!MethodHandle.class.equals(type)) {
                throw new IllegalArgumentException(type.toString());
            }
        }

        List<MethodHandle> getterList = List.of(getters);
        MethodHandle handle;
        switch (methodName) {
            case "equals":
                if (methodType != null && !methodType.equals(MethodType.methodType(Boolean.TYPE, recordClass, Object.class))) {
                    throw new IllegalArgumentException("Bad method type: " + methodType);
                }

                handle = makeEquals(recordClass, getterList);
                break;
            case "hashCode":
                if (methodType != null && !methodType.equals(MethodType.methodType(Integer.TYPE, recordClass))) {
                    throw new IllegalArgumentException("Bad method type: " + methodType);
                }

                handle = makeHashCode(recordClass, getterList);
                break;
            case "toString":
                if (methodType != null && !(methodType.equals(MethodType.methodType(IASString.class, recordClass)) || methodType.equals(MethodType.methodType(String.class, recordClass)))) {
                    throw new IllegalArgumentException("Bad method type: " + methodType);
                }


                List<String> nameList = "".equals(names) ? List.of() : List.of(names.split(";"));
                if (nameList.size() != getterList.size()) {
                    throw new IllegalArgumentException("Name list and accessor list do not match");
                }
                if(methodType.returnType().equals(IASString.class)) {
                    handle = makeToTString(recordClass, getterList, nameList);
                } else {
                    handle = makeToString(recordClass, getterList, nameList);
                }
                break;
            default:
                throw new IllegalArgumentException(methodName);
        }

        return methodType != null ? new ConstantCallSite(handle) : handle;
    }

    static {
        FALSE = MethodHandles.constant(Boolean.TYPE, false);
        TRUE = MethodHandles.constant(Boolean.TYPE, true);
        ZERO = MethodHandles.constant(Integer.TYPE, 0);
        primitiveEquals = new HashMap<>();
        primitiveHashers = new HashMap<>();
        primitiveToString = new HashMap<>();
        primitiveToTString = new HashMap<>();
        try {
            Class<ObjectMethods> OBJECT_METHODS_CLASS = ObjectMethods.class;
            MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            ClassLoader loader = ClassLoader.getPlatformClassLoader();

            CLASS_IS_INSTANCE = publicLookup.findVirtual(Class.class, "isInstance", MethodType.methodType(Boolean.TYPE, Object.class));
            OBJECT_EQUALS = publicLookup.findVirtual(Object.class, "equals", MethodType.methodType(Boolean.TYPE, Object.class));
            OBJECT_HASHCODE = publicLookup.findVirtual(Object.class, "hashCode", MethodType.fromMethodDescriptorString("()I", loader));
            OBJECT_TO_STRING = publicLookup.findVirtual(Object.class, "toString", MethodType.methodType(String.class));
            TSTRING_FORMAT = publicLookup.findStatic(IASString.class, "format", MethodType.methodType(IASString.class, IASString.class, Object[].class));
            STRING_FORMAT = publicLookup.findStatic(String.class, "format", MethodType.methodType(String.class, String.class, Object[].class));
            OBJECTS_EQUALS = publicLookup.findStatic(Objects.class, "equals", MethodType.methodType(Boolean.TYPE, Object.class, Object.class));
            OBJECTS_HASHCODE = publicLookup.findStatic(Objects.class, "hashCode", MethodType.methodType(Integer.TYPE, Object.class));
            OBJECTS_TOSTRING = publicLookup.findStatic(Objects.class, "toString", MethodType.methodType(String.class, Object.class));
            OBJECTS_TOTSTRING = publicLookup.findStatic(com.sap.fontus.taintaware.unified.runtime.Objects.class, "toString", MethodType.methodType(IASString.class, Object.class));
            OBJECT_EQ = lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.methodType(Boolean.TYPE, Object.class, Object.class));
            HASH_COMBINER = lookup.findStatic(OBJECT_METHODS_CLASS, "hashCombiner", MethodType.fromMethodDescriptorString("(II)I", loader));
            primitiveEquals.put(Byte.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(BB)Z", loader)));
            primitiveEquals.put(Short.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(SS)Z", loader)));
            primitiveEquals.put(Character.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(CC)Z", loader)));
            primitiveEquals.put(Integer.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(II)Z", loader)));
            primitiveEquals.put(Long.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(JJ)Z", loader)));
            primitiveEquals.put(Float.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(FF)Z", loader)));
            primitiveEquals.put(Double.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(DD)Z", loader)));
            primitiveEquals.put(Boolean.TYPE, lookup.findStatic(OBJECT_METHODS_CLASS, "eq", MethodType.fromMethodDescriptorString("(ZZ)Z", loader)));
            primitiveHashers.put(Byte.TYPE, lookup.findStatic(Byte.class, "hashCode", MethodType.fromMethodDescriptorString("(B)I", loader)));
            primitiveHashers.put(Short.TYPE, lookup.findStatic(Short.class, "hashCode", MethodType.fromMethodDescriptorString("(S)I", loader)));
            primitiveHashers.put(Character.TYPE, lookup.findStatic(Character.class, "hashCode", MethodType.fromMethodDescriptorString("(C)I", loader)));
            primitiveHashers.put(Integer.TYPE, lookup.findStatic(Integer.class, "hashCode", MethodType.fromMethodDescriptorString("(I)I", loader)));
            primitiveHashers.put(Long.TYPE, lookup.findStatic(Long.class, "hashCode", MethodType.fromMethodDescriptorString("(J)I", loader)));
            primitiveHashers.put(Float.TYPE, lookup.findStatic(Float.class, "hashCode", MethodType.fromMethodDescriptorString("(F)I", loader)));
            primitiveHashers.put(Double.TYPE, lookup.findStatic(Double.class, "hashCode", MethodType.fromMethodDescriptorString("(D)I", loader)));
            primitiveHashers.put(Boolean.TYPE, lookup.findStatic(Boolean.class, "hashCode", MethodType.fromMethodDescriptorString("(Z)I", loader)));
            primitiveToString.put(Byte.TYPE, lookup.findStatic(Byte.class, "toString", MethodType.methodType(String.class, Byte.TYPE)));
            primitiveToString.put(Short.TYPE, lookup.findStatic(Short.class, "toString", MethodType.methodType(String.class, Short.TYPE)));
            primitiveToString.put(Character.TYPE, lookup.findStatic(Character.class, "toString", MethodType.methodType(String.class, Character.TYPE)));
            primitiveToString.put(Integer.TYPE, lookup.findStatic(Integer.class, "toString", MethodType.methodType(String.class, Integer.TYPE)));
            primitiveToString.put(Long.TYPE, lookup.findStatic(Long.class, "toString", MethodType.methodType(String.class, Long.TYPE)));
            primitiveToString.put(Float.TYPE, lookup.findStatic(Float.class, "toString", MethodType.methodType(String.class, Float.TYPE)));
            primitiveToString.put(Double.TYPE, lookup.findStatic(Double.class, "toString", MethodType.methodType(String.class, Double.TYPE)));
            primitiveToString.put(Boolean.TYPE, lookup.findStatic(Boolean.class, "toString", MethodType.methodType(String.class, Boolean.TYPE)));

            primitiveToTString.put(Byte.TYPE, lookup.findStatic(IASStringUtils.class, "byteToString", MethodType.methodType(IASString.class, Byte.TYPE)));
            primitiveToTString.put(Short.TYPE, lookup.findStatic(IASStringUtils.class, "shortToString", MethodType.methodType(IASString.class, Short.TYPE)));
            primitiveToTString.put(Character.TYPE, lookup.findStatic(IASStringUtils.class, "characterToString", MethodType.methodType(IASString.class, Character.TYPE)));
            primitiveToTString.put(Integer.TYPE, lookup.findStatic(IASStringUtils.class, "intToString", MethodType.methodType(IASString.class, Integer.TYPE)));
            primitiveToTString.put(Long.TYPE, lookup.findStatic(IASStringUtils.class, "longToString", MethodType.methodType(IASString.class, Long.TYPE)));
            primitiveToTString.put(Float.TYPE, lookup.findStatic(IASStringUtils.class, "floatToString", MethodType.methodType(IASString.class, Float.TYPE)));
            primitiveToTString.put(Double.TYPE, lookup.findStatic(IASStringUtils.class, "doubleToString", MethodType.methodType(IASString.class, Double.TYPE)));
            primitiveToTString.put(Boolean.TYPE, lookup.findStatic(IASStringUtils.class, "booleanToString", MethodType.methodType(IASString.class, Boolean.TYPE)));

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
