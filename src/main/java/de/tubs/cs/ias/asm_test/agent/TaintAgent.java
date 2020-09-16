package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.config.Configuration;

import java.lang.instrument.Instrumentation;

public class TaintAgent {
    public static void premain(String args, Instrumentation inst) {
        Configuration.parseAgent(args);
        inst.addTransformer(new TaintingTransformer(Configuration.getConfiguration(), inst));
    }
}
