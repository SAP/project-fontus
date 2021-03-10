package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;

public class PropertiesStrategy extends AbstractInstrumentation {
    public PropertiesStrategy(TaintStringConfig taintStringConfig) {
        super(Constants.PropertyDesc, taintStringConfig.getTPropertiesDesc(), Constants.PropertyQN, taintStringConfig.getTPropertiesQN(), Constants.TPropertiesToPropertiesName);
    }
}
