package com.sap.fontus.agent;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.VerboseLogger;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Objects;

public class TaintAgent {
    private static Instrumentation instrumentation;
    private static TaintingTransformer transformer;
    private static HashMap<String, Class<?>> loadedClasses = new HashMap<>();

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        InstrumentationConfiguration.init(null, null);
        Configuration.parseAgent(args);

        if (Configuration.getConfiguration().isShowWelcomeMessage()) {
            System.out.println("Starting application with Fontus Tainting!");
            System.out.println("  * Loaded " + Configuration.getConfiguration().summary());
        }

        transformer = new TaintingTransformer(Configuration.getConfiguration());
        inst.addTransformer(transformer);
    }

    public static void logInstrumentedClass(String qn) {
        byte[] data = transformer.findInstrumentedClass(qn);
        VerboseLogger.save(qn, data);
    }

    public static Class<?> findLoadedClass(String className) {
        return findLoadedClassOptimized(className);
    }

    public static Class<?> findLoadedClassOptimized(String className) {
        Objects.requireNonNull(className);

        // Bypass for offline and tests
        if (instrumentation == null) {
            try {
                return Class.forName(className, false, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        Class<?> cls = loadedClasses.get(className);

        if (cls == null) {
            for (Class<?> newCls : instrumentation.getAllLoadedClasses()) {
                if (!loadedClasses.containsKey(newCls.getName())) {
                    loadedClasses.put(newCls.getName(), newCls);
                }
            }

            cls = loadedClasses.get(className);
        }
        return cls;
    }
}
