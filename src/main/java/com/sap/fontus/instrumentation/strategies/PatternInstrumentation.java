package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;

public class PatternInstrumentation extends AbstractInstrumentation {
    public PatternInstrumentation(TaintStringConfig taintStringConfig) {
        super(Constants.PatternDesc, taintStringConfig.getTPatternDesc(), Constants.PatternQN, taintStringConfig.getTPatternQN(), Constants.TPatternToPatternName);
    }
}
