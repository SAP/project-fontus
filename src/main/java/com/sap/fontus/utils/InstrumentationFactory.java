package com.sap.fontus.utils;

import com.sap.fontus.agent.InstrumentationConfiguration;
import com.sap.fontus.asm.ClassResolver;
import com.sap.fontus.asm.IClassResolver;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.offline.OfflineClassResolver;

public class InstrumentationFactory {
    private static OfflineClassResolver offlineClassResolver = new OfflineClassResolver();

    public static IClassResolver createClassResolver(ClassLoader classLoader) {
        if (Configuration.getConfiguration().isOfflineInstrumentation()) {
            return offlineClassResolver;
        } else {
            return new ClassResolver(classLoader);
        }
    }
}
