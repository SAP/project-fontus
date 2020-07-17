package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

public class MatcherInstrumentation extends AbstractInstrumentation{
    public MatcherInstrumentation(TaintStringConfig taintStringConfig) {
        super(Constants.MatcherDesc, taintStringConfig.getTMatcherDesc(), Constants.MatcherQN, taintStringConfig.getTMatcherQN(), Constants.TMatcherToMatcherName);
    }
}
