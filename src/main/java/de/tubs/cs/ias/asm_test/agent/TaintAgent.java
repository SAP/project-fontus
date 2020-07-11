package de.tubs.cs.ias.asm_test.agent;

import de.tubs.cs.ias.asm_test.config.Configuration;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.lang.instrument.Instrumentation;
import de.tubs.cs.ias.asm_test.utils.Logger;

public class TaintAgent {
    public static void premain(String args, Instrumentation inst) {
        Configuration.parseAgent(args);
        inst.addTransformer(new TaintingTransformer(Configuration.getConfiguration()));
    }
}
