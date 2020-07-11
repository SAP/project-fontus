package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.lang.instrument.Instrumentation;
import de.tubs.cs.ias.asm_test.utils.Logger;

public class TaintAgent {
    private static final Logger logger = LogUtils.getLogger();
    private static Configuration configuration;

    public static void premain(String args, Instrumentation inst) {
        configuration = AgentConfig.parseConfig(args);
        inst.addTransformer(new TaintingTransformer(configuration));
    }

    public static Configuration getConfiguration() {
        // TODO Temporary fix for offline instrumentation
        if (configuration == null) {
            return new Configuration();
        }
        return configuration;
    }
}
