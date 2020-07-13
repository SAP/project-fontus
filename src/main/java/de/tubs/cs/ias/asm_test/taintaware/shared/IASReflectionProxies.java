package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;

public class IASReflectionProxies {
    private static final TaintStringConfig tsc = new TaintStringConfig(Configuration.getConfiguration().getTaintMethod());

    public static Class<?> classForName(IASStringable str) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = InstrumentationHelper.getInstance(tsc).translateClassName(s);
        return Class.forName(clazz);
    }

    public static Class<?> classForName(IASStringable str, boolean initialize,
                                        ClassLoader loader) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = InstrumentationHelper.getInstance(tsc).translateClassName(s);
        return Class.forName(clazz, initialize, loader);
    }
}
