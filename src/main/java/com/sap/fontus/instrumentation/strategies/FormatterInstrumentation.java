package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;

public class FormatterInstrumentation extends AbstractInstrumentation {
    private final TaintStringConfig stringConfig;

    public FormatterInstrumentation(TaintStringConfig configuration) {
        super(Constants.FormatterDesc, configuration.getTFormatterDesc(), Constants.FormatterQN, configuration.getTFormatterQN(), Constants.TFormatterToFormatterName);
        this.stringConfig = configuration;
    }

    @Override
    public String instrumentDescForIASCall(String desc) {
        String parameters = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        parameters = Constants.formatterPattern.matcher(parameters).replaceAll(this.stringConfig.getMethodTFormatterDesc());
        String returnType = desc.substring(desc.indexOf(")") + 1);
        returnType = Constants.formatterPattern.matcher(returnType).replaceAll(this.stringConfig.getTFormatterDesc());
        return desc.substring(0, desc.indexOf("(") + 1) + parameters + ")" + returnType;
    }
}
