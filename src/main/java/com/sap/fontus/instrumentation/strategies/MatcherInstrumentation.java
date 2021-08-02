package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASMatcher;
import org.objectweb.asm.Type;

import java.util.regex.Matcher;

public class MatcherInstrumentation extends AbstractInstrumentation{
    public MatcherInstrumentation(InstrumentationHelper instrumentationHelper) {
        super(Type.getType(Matcher.class), Type.getType(IASMatcher.class), instrumentationHelper, Constants.TMatcherToMatcherName);
    }
}
