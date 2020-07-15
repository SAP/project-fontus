package de.tubs.cs.ias.asm_test.taintaware.shared;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import jdk.internal.reflect.Reflection;

public class IASReflectionProxies {
    private static final TaintStringConfig tsc = new TaintStringConfig(Configuration.getConfiguration().getTaintMethod());

    @SuppressWarnings("Since15")
    public static Class<?> classForName(IASStringable str) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = InstrumentationHelper.getInstance(tsc).translateClassName(s);

        // Get caller class classloader
        Class callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = Reflection.getCallerClass();
        }
        ClassLoader cl = callerClass.getClassLoader();

        return Class.forName(clazz, true, cl);
    }

    public static Class<?> classForName(IASStringable str, boolean initialize,
                                        ClassLoader loader) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = InstrumentationHelper.getInstance(tsc).translateClassName(s);
        return Class.forName(clazz, initialize, loader);
    }
}
