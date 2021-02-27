package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;

public class StringInstrumentation extends AbstractInstrumentation {
    protected final TaintStringConfig stringConfig;

    public StringInstrumentation(TaintStringConfig configuration) {
        super(Constants.StringDesc, configuration.getTStringDesc(), Constants.StringQN, configuration.getTStringQN(), Constants.TStringToStringName);
        this.stringConfig = configuration;
    }

    @Override
    public String instrumentDesc(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.strPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTStringDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.strPattern.matcher(returnType).replaceAll(this.stringConfig.getTStringDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }
}
