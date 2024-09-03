package com.sap.fontus.utils;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;

public class UnsafeUtils {
    private static final Unsafe UNSAFE;
    private static final Collection<AccessibleObject> setAccessible;

    static {
        try {
            UNSAFE = Unsafe.getUnsafe();
        } catch (Throwable ex) {
            System.err.println("Couldn't load unsafe! Please make sure you added \"--add-opens java.base/jdk.internal.misc=ALL-UNNAMED\" as JVM parameter!");
            ex.printStackTrace();
            System.exit(1);
            throw ex;
        }
        setAccessible = getSetAccessible();
    }


    private static Collection<AccessibleObject> getSetAccessible() {
        try(InputStream is = UnsafeUtils.class.getClassLoader().getResourceAsStream("SetAccessible.bytes")) {
            byte[] bytes = Objects.requireNonNull(is).readAllBytes();
            Class<Collection<AccessibleObject>> collectionClass = null;
            try {
                Method defineAnonymousClass = UNSAFE.getClass().getMethod("defineAnonymousClass", Class.class, byte[].class,
                        Object[].class);
                @SuppressWarnings("unchecked")
                Class<Collection<AccessibleObject>> unchecked = (Class<Collection<AccessibleObject>>) defineAnonymousClass
                        .invoke(UNSAFE, URL.class, bytes, null);
                collectionClass = unchecked;

            } catch (NoSuchMethodException e) {
                long offset = (long) UNSAFE.getClass().getMethod("staticFieldOffset", Field.class).invoke(UNSAFE,
                        MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP"));
                MethodHandles.Lookup lookup = (MethodHandles.Lookup) UNSAFE.getClass()
                        .getMethod("getObject", Object.class, long.class)
                        .invoke(UNSAFE, MethodHandles.Lookup.class, offset);
                lookup = lookup.in(URL.class);
                Class<?> classOption = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
                Object classOptions = Array.newInstance(classOption, 0);
                Method defineHiddenClass = MethodHandles.Lookup.class.getMethod("defineHiddenClass", byte[].class, boolean.class,
                        classOptions.getClass());
                lookup = (MethodHandles.Lookup) defineHiddenClass.invoke(lookup, bytes, Boolean.FALSE, classOptions);
                @SuppressWarnings("unchecked")
                Class<Collection<AccessibleObject>> unchecked = (Class<Collection<AccessibleObject>>) lookup
                        .lookupClass();
                collectionClass = unchecked;
            }

            return collectionClass.getConstructor().newInstance();
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /*@ForceInline
    public static Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        return UNSAFE.defineAnonymousClass(hostClass, data, cpPatches);
    }*/

    @ForceInline
    public static Class<?> defineClass(String name, byte[] bytes, ClassLoader classLoader) {
        return UNSAFE.defineClass(name, bytes, 0, bytes.length, classLoader, null);
    }

    @ForceInline
    public static void setAccessible(AccessibleObject ao) {
        setAccessible.add(ao);
    }
}
