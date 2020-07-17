package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

public class PropertiesStrategy extends AbstractInstrumentation {
    public PropertiesStrategy(TaintStringConfig taintStringConfig) {
        super(Constants.PropertyDesc, taintStringConfig.getTPropertiesDesc(), Constants.PropertyQN, taintStringConfig.getTPropertiesQN(), Constants.TPropertiesToPropertiesName);
    }
}
