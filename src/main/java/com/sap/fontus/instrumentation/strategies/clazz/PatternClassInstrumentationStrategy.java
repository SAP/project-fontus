package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class PatternClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public PatternClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig taintStringConfig) {
        super(visitor, Constants.PatternDesc, taintStringConfig.getTPatternDesc(), Constants.PatternQN, Constants.TPatternToPatternName);
    }
}
