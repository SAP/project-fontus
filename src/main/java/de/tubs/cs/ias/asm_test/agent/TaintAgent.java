package de.tubs.cs.ias.asm_test.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tubs.cs.ias.asm_test.config.Configuration;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;

public class TaintAgent {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Configuration configuration;

    public static void premain(String args, Instrumentation inst) {
        configuration = AgentConfig.parseConfig(args);
        inst.addTransformer(new TaintingTransformer(configuration));
    }

    public static Configuration getConfiguration() {
        return configuration;
    }
}
