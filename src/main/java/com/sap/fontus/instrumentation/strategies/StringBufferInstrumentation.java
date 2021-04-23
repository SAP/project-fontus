package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;

import java.util.regex.Pattern;

public class StringBufferInstrumentation extends AbstractInstrumentation {
    private static final Pattern STRING_BUFFER_QN_MATCHER = Pattern.compile(Constants.StringBufferQN, Pattern.LITERAL);
    private final TaintStringConfig stringConfig;

    public StringBufferInstrumentation(TaintStringConfig configuration) {
        super(Constants.StringBufferDesc, configuration.getTStringBufferDesc(), Constants.StringBufferQN, configuration.getTStringBufferQN(), Constants.TStringBufferToStringBufferName);
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
