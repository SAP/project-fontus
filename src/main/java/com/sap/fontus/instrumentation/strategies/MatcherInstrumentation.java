package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import org.objectweb.asm.Type;

import java.util.regex.Matcher;

public class MatcherInstrumentation extends AbstractInstrumentation{
    public MatcherInstrumentation(TaintStringConfig taintStringConfig, InstrumentationHelper instrumentationHelper) {
        super(Type.getType(Matcher.class), Type.getType(taintStringConfig.getTMatcherDesc()), instrumentationHelper, Constants.TMatcherToMatcherName);
    }
}
