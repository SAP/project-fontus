package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class StringBuilderClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public StringBuilderClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig config) {
        super(visitor, Constants.StringBuilderDesc, config.getTStringBuilderDesc(), Constants.StringBuilderQN, Constants.TStringBuilderToStringBuilderName);
    }
}