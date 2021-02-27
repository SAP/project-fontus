package com.sap.fontus.agent;

import com.sap.fontus.config.Configuration;

import java.lang.instrument.Instrumentation;
import java.util.Objects;

public class TaintAgent {
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        Configuration.parseAgent(args);
        inst.addTransformer(new TaintingTransformer(Configuration.getConfiguration()));
    }

    public static Class<?> findLoadedClass(String className) {
        Objects.requireNonNull(className);

        // Bypass for offline and tests
        if (instrumentation == null) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        for (Class<?> cls : instrumentation.getAllLoadedClasses()) {
            if (className.equals(cls.getName())) {
                return cls;
            }
        }
        return null;
    }
}
