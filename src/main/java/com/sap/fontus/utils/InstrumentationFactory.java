package com.sap.fontus.utils;

import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.IClassResolver;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.offline.OfflineClassResolver;

public class InstrumentationFactory {
    private static final OfflineClassResolver offlineClassResolver;
    private static final ClassResolver classResolver;

    static {
        classResolver = new ClassResolver();
        offlineClassResolver = new OfflineClassResolver(classResolver);

        classResolver.addClassLoader(ClassLoader.getSystemClassLoader());
    }

    public static IClassResolver createClassResolver(ClassLoader classLoader) {
        if (Configuration.getConfiguration().isOfflineInstrumentation()) {
            return offlineClassResolver;
        } else {
            classResolver.addClassLoader(classLoader);
            return classResolver;
        }
    }
}
