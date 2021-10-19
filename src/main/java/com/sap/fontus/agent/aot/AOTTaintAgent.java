package com.sap.fontus.agent.aot;


import com.sap.fontus.agent.TaintAgent;

import java.lang.instrument.Instrumentation;

public class AOTTaintAgent {

    public static void premain(String args, Instrumentation inst) {
        TaintAgent.premain(args, inst);
    }
}
