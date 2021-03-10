package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.PatternInstrumentation;
import org.objectweb.asm.MethodVisitor;

import java.util.regex.Pattern;

public class PatternMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public PatternMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig taintStringConfig) {
        super(parentVisitor, taintStringConfig.getTPatternDesc(), taintStringConfig.getTPatternQN(), Constants.TPatternToPatternName, Pattern.class, taintStringConfig, new PatternInstrumentation(taintStringConfig));
    }
}
