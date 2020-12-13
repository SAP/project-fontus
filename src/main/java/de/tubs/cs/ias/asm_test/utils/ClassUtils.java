package de.tubs.cs.ias.asm_test.utils;

import de.tubs.cs.ias.asm_test.config.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassUtils {
    private static final Method findLoadedClass;

    static {
        Method findLoadedClass1 = null;
        try {
            findLoadedClass1 = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        findLoadedClass = findLoadedClass1;
        findLoadedClass.setAccessible(true);
    }

    public static Class<?> findLoadedClass(String internalName) {
        try {
            return (Class<?>) findLoadedClass.invoke(Thread.currentThread().getContextClassLoader(), Utils.slashToDot(internalName));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getClassInputStream(String internalName, ClassLoader loader) {
        String resourceName = internalName + ".class";
        InputStream resource = ClassLoader.getSystemResourceAsStream(resourceName);
        if (resource == null) {
            resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            if (resource == null) {
                if (loader != null) {
                    resource = loader.getResourceAsStream(resourceName);
                }
            }
        }
        if (resource != null) {
            return resource;
        }
        throw new RuntimeException("Resource for " + internalName + "couldn't be found");
    }

    public static boolean isInterface(String internalName) {
        return ClassUtils.isInterface(internalName, null);
    }

    public static boolean isInterface(int access) {
        return ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
    }

    public static boolean isInterface(byte[] bytes) {
        return ClassUtils.isInterface(new ClassReader(bytes).getAccess());
    }

    public static boolean isInterface(String internalName, ClassLoader loader) {
        try {
            return ClassUtils.isInterface(new ClassReader(getClassInputStream(internalName, loader)).getAccess());
        } catch (IOException e) {
            if (Configuration.isLoggingEnabled()) {
                System.err.println("Could not resolve class " + internalName + " for isInterface checking");
            }
        }
        return false;
    }
}
