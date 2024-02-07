package com.sap.fontus.utils;

import com.sap.fontus.asm.resolver.ClassResolverFactory;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.Optional;

public final class ClassUtils {
    private static final Logger logger = LogUtils.getLogger();
    public static final CombinedExcludedLookup combinedExcludedLookup = new CombinedExcludedLookup(null);
    private static final ClassFinder classFinder = ClassResolverFactory.createClassFinder();

    private ClassUtils() {
    }

    public static Class<?> findLoadedClass(String internalName) {
        Class<?> loaded = classFinder.findClass(Utils.slashToDot(internalName));
        if (loaded == null && combinedExcludedLookup.isJdkClass(internalName)) {
            try {
                loaded = Class.forName(Type.getObjectType(internalName).getClassName());
            } catch (ClassNotFoundException e) {
                Utils.logException(e);
            }
        }
        return loaded;
    }

    public static Class<?> findLoadedClass(String internalName, ClassLoader loader) {
        Class<?> loaded = classFinder.findClass(Utils.slashToDot(internalName));
        if (loaded == null && new CombinedExcludedLookup(loader).isJdkClass(internalName)) {
            try {
                loaded = Class.forName(Type.getObjectType(internalName).getClassName(), false, loader);
            } catch (ClassNotFoundException e) {
                Utils.logException(e);
            }
        }
        return loaded;
    }

    public static byte[] getClassBytes(String internalName, ClassLoader loader) {
        return ClassResolverFactory
                .createClassResolver(loader)
                .resolve(internalName)
                .orElseThrow(() -> new RuntimeException("Resource for " + internalName + " couldn't be found"));
    }

    public static boolean isInterface(String internalName) {
        return isInterface(internalName, null);
    }

    public static boolean isInterface(int access) {
        return ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
    }

    public static boolean isInterface(byte[] bytes) {
        return isInterface(new ClassReader(bytes).getAccess());
    }

    public static boolean isInterface(String internalName, ClassLoader loader) {
        Optional<byte[]> bytes = ClassResolverFactory
                .createClassResolver(loader)
                .resolve(internalName);
        if (bytes.isPresent()) {
            return isInterface(new ClassReader(bytes.get()).getAccess());
        } else {
            logger.error("Could not resolve class " + internalName + " for isInterface checking");
            return false;
        }
    }

    public static Class<?> arrayType(Class<?> cls) {
        return Array.newInstance(cls, 0).getClass();
    }
}
