package com.sap.fontus.utils;

import com.sap.fontus.agent.TaintAgent;
import com.sap.fontus.config.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;

public class ClassUtils {
    public static Class<?> findLoadedClass(String internalName) {
        return TaintAgent.findLoadedClass(Utils.slashToDot(internalName));
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
