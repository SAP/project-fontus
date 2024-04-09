package com.sap.fontus.agent;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.utils.LogUtils;
import com.sap.fontus.utils.VerboseLogger;

import java.lang.instrument.Instrumentation;

public class TaintAgent {
    private static Instrumentation instrumentation;
    private static TaintingTransformer transformer;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        InstrumentationConfiguration.init(null, null);
        Configuration.parseAgent(args);

        Configuration.getConfiguration().validate();

        if(Configuration.isLoggingEnabled()) {
            LogUtils.LOGGING_ENABLED = true;
        }
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

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
