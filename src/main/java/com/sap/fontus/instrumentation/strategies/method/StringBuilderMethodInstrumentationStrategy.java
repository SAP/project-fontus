package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.StringBuilderInstrumentation;
import org.objectweb.asm.MethodVisitor;

public class StringBuilderMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public StringBuilderMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(mv, configuration.getTStringBuilderDesc(), configuration.getTStringBuilderQN(), Constants.TStringBuilderToStringBuilderName, StringBuilder.class, configuration, new StringBuilderInstrumentation(configuration));
    }
}
