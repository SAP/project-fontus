package com.sap.fontus.taintaware.shared;

import com.sap.fontus.Constants;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.utils.ReflectionUtils;

public class IASClassProxy {
    private static final TaintStringConfig tsc = new TaintStringConfig(Configuration.getConfiguration().getTaintMethod());
    private static final InstrumentationHelper instrumentationHelper = new InstrumentationHelper(tsc);

    @SuppressWarnings("Since15")
    public static Class<?> forName(IASStringable str) throws ClassNotFoundException {
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
    public static Class<?> forName(IASStringable str, boolean initialize,
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
