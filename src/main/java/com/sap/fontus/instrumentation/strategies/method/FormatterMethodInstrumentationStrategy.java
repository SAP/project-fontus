package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.FormatterInstrumentation;
import org.objectweb.asm.MethodVisitor;

import java.util.Formatter;

public class FormatterMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public FormatterMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig stringConfig) {
        super(parentVisitor, stringConfig.getTFormatterDesc(), stringConfig.getTFormatterQN(), Constants.TFormatterToFormatterName, Formatter.class, stringConfig, new FormatterInstrumentation(stringConfig));
    }
}
