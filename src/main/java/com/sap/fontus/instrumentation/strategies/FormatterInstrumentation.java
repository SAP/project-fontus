package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASFormatter;
import org.objectweb.asm.Type;

import java.util.Formatter;

public class FormatterInstrumentation extends AbstractInstrumentation {
    public FormatterInstrumentation(InstrumentationHelper instrumentationHelper) {
        super(Type.getType(Formatter.class), Type.getType(IASFormatter.class), instrumentationHelper, Constants.TFormatterToFormatterName);
    }
}
