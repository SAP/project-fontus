package com.sap.fontus.taintaware.unified;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.utils.ReflectionUtils;

public class IASClassProxy {
    private static final InstrumentationHelper instrumentationHelper = new InstrumentationHelper();

    @SuppressWarnings("Since15")
    public static Class<?> forName(IASString str) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = instrumentationHelper.translateClassName(s).orElse(s);

        // Get caller class classloader
        Class<?> callerClass;
        if (Constants.JAVA_VERSION >= 9) {
            callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
        } else {
            callerClass = ReflectionUtils.getCallerClass();
        }
        ClassLoader cl = callerClass.getClassLoader();

        return Class.forName(clazz, true, cl);
    }

    @SuppressWarnings("Since15")
    public static Class<?> forName(IASString str, boolean initialize,
                                   ClassLoader loader) throws ClassNotFoundException {
        String s = str.getString();
        String clazz = instrumentationHelper.translateClassName(s).orElse(s);

        if(loader == null) {
            // Get caller class classloader
            Class<?> callerClass;
            if (Constants.JAVA_VERSION >= 9) {
                callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .getCallerClass();
            } else {
                callerClass = ReflectionUtils.getCallerClass();
            }
            loader = callerClass.getClassLoader();
        }

        return Class.forName(clazz, initialize, loader);
    }
}
