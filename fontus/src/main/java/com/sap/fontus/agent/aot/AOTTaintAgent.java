package com.sap.fontus.agent.aot;


import com.sap.fontus.agent.TaintAgent;

import java.lang.instrument.Instrumentation;

public final class AOTTaintAgent {

    private AOTTaintAgent() {}

    public static void premain(String args, Instrumentation inst) {
        TaintAgent.premain(args, inst);
    }
}
