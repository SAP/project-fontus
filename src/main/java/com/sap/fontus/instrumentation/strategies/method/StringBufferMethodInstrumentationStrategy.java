package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.StringBufferInstrumentation;
import org.objectweb.asm.MethodVisitor;


public class StringBufferMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public StringBufferMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(mv, configuration.getTStringBufferDesc(), configuration.getTStringBufferQN(), Constants.TStringBufferToStringBufferName, StringBuffer.class, configuration, new StringBufferInstrumentation(configuration));
    }
}
