package com.sap.fontus.utils;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.net.URL;
import java.util.Collection;

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
            byte[] bytes = is.readAllBytes();
            Class<Collection<AccessibleObject>> collectionClass = null;
            try {
                Method defineAnonymousClass = UNSAFE.getClass().getMethod("defineAnonymousClass", Class.class, byte[].class, //$NON-NLS-1$
                        Object[].class);
                @SuppressWarnings("unchecked")
                Class<Collection<AccessibleObject>> unchecked = (Class<Collection<AccessibleObject>>) defineAnonymousClass
                        .invoke(UNSAFE, URL.class, bytes, (Object[]) null);
                collectionClass = unchecked;

            } catch (NoSuchMethodException e) {
                long offset = (long) UNSAFE.getClass().getMethod("staticFieldOffset", Field.class).invoke(UNSAFE, //$NON-NLS-1$
                        MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP")); //$NON-NLS-1$
                MethodHandles.Lookup lookup = (MethodHandles.Lookup) UNSAFE.getClass()
                        .getMethod("getObject", Object.class, long.class) //$NON-NLS-1$
                        .invoke(UNSAFE, MethodHandles.Lookup.class, offset);
                lookup = lookup.in(URL.class);
                Class<?> classOption = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption"); //$NON-NLS-1$
                Object classOptions = Array.newInstance(classOption, 0);
                Method defineHiddenClass = MethodHandles.Lookup.class.getMethod("defineHiddenClass", byte[].class, boolean.class, //$NON-NLS-1$
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
    /**
     * Defines a class but does not make it known to the class loader or system dictionary.
     * <p>
     * For each CP entry, the corresponding CP patch must either be null or have
     * the a format that matches its tag:
     * <ul>
     * <li>Integer, Long, Float, Double: the corresponding wrapper object type from java.lang
     * <li>Utf8: a string (must have suitable syntax if used as signature or name)
     * <li>Class: any java.lang.Class object
     * <li>String: any object (not just a java.lang.String)
     * <li>InterfaceMethodRef: (NYI) a method handle to invoke on that call site's arguments
     * </ul>
     * @param hostClass context for linkage, access control, protection domain, and class loader
     * @param data      bytes of a class file
     * @param cpPatches where non-null entries exist, they replace corresponding CP entries in data
     */
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
