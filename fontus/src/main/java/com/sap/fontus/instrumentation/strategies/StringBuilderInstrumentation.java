package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASStringBuilder;
import org.objectweb.asm.Type;

public class StringBuilderInstrumentation extends AbstractInstrumentation {
    public StringBuilderInstrumentation(InstrumentationHelper instrumentationHelper) {
        super(Type.getType(StringBuilder.class), Type.getType(IASStringBuilder.class), instrumentationHelper, Constants.TStringBuilderToStringBuilderName);
    }
}
