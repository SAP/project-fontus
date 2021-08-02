package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASStringBuffer;
import org.objectweb.asm.Type;

public class StringBufferInstrumentation extends AbstractInstrumentation {
    public StringBufferInstrumentation(InstrumentationHelper instrumentationHelper) {
        super(Type.getType(StringBuffer.class), Type.getType(IASStringBuffer.class), instrumentationHelper, Constants.TStringBufferToStringBufferName);
    }
}
