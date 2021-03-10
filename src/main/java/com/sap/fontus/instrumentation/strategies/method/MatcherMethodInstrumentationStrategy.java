package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.MatcherInstrumentation;
import org.objectweb.asm.MethodVisitor;

import java.util.regex.Matcher;

public class MatcherMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {

    public MatcherMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig taintStringConfig) {
        super(parentVisitor, taintStringConfig.getTMatcherDesc(), taintStringConfig.getTMatcherQN(), Constants.TMatcherToMatcherName, Matcher.class, taintStringConfig, new MatcherInstrumentation(taintStringConfig));
    }
}
