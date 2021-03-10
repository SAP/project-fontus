package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

import java.util.regex.Matcher;

public class MatcherClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public MatcherClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig taintStringConfig) {
        super(visitor, Matcher.class, taintStringConfig.getTMatcherDesc(), taintStringConfig.getTMatcherQN(), Constants.TMatcherToMatcherName);
    }
}
