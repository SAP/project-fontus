package de.tubs.cs.ias.asm_test.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandles;

public class TaintAgent {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void premain(String args, Instrumentation inst) {
        boolean verbose = false;
        if(args != null && args.equals("verbose")) {
            verbose = true;
        }
        inst.addTransformer(new TaintingTransformer(verbose));
    }


}
