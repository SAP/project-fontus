package de.tubs.cs.ias.asm_test.taintaware;

import de.tubs.cs.ias.asm_test.strategies.ClassNameTranslator;

public class IASReflectionProxies {
    public static Class<?> classForName(IASString str) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = ClassNameTranslator.translateClassName(s);
        return Class.forName(clazz);
    }

    public static Class<?> classForName(IASString str, boolean initialize,
                                        ClassLoader loader) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = ClassNameTranslator.translateClassName(s);
        return Class.forName(clazz, initialize, loader);
    }
}
