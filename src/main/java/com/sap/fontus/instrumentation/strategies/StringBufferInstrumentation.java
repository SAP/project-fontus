package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import org.objectweb.asm.Type;

public class StringBufferInstrumentation extends AbstractInstrumentation {
    private final TaintStringConfig stringConfig;

    public StringBufferInstrumentation(TaintStringConfig configuration, InstrumentationHelper instrumentationHelper) {
        super(Type.getType(StringBuffer.class), Type.getType(configuration.getTStringBufferDesc()), instrumentationHelper, Constants.TStringBufferToStringBufferName);
        this.stringConfig = configuration;
    }

    @Override
    public String instrumentDescForIASCall(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.strBufferPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTStringBufferDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.strBufferPattern.matcher(returnType).replaceAll(this.stringConfig.getTStringBufferDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }
}
