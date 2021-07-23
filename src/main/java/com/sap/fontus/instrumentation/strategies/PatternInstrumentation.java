package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import org.objectweb.asm.Type;

import java.util.regex.Pattern;

public class PatternInstrumentation extends AbstractInstrumentation {
    public PatternInstrumentation(TaintStringConfig taintStringConfig, InstrumentationHelper instrumentationHelper) {
        super(Type.getType(Pattern.class), Type.getType(taintStringConfig.getTPatternDesc()), instrumentationHelper, Constants.TPatternToPatternName);
    }
}
