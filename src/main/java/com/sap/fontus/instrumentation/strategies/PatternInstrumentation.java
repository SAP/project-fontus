package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASPattern;
import org.objectweb.asm.Type;

import java.util.regex.Pattern;

public class PatternInstrumentation extends AbstractInstrumentation {
    public PatternInstrumentation(InstrumentationHelper instrumentationHelper) {
        super(Type.getType(Pattern.class), Type.getType(IASPattern.class), instrumentationHelper, Constants.TPatternToPatternName);
    }
}
