package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;

public class MatcherInstrumentation extends AbstractInstrumentation{
    public MatcherInstrumentation(TaintStringConfig taintStringConfig) {
        super(Constants.MatcherDesc, taintStringConfig.getTMatcherDesc(), Constants.MatcherQN, taintStringConfig.getTMatcherQN(), Constants.TMatcherToMatcherName);
    }
}
