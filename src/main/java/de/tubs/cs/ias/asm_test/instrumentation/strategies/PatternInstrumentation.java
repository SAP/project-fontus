package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;

public class PatternInstrumentation extends AbstractInstrumentation {
    public PatternInstrumentation(TaintStringConfig taintStringConfig) {
        super(Constants.PatternDesc, taintStringConfig.getTPatternDesc(), Constants.PatternQN, taintStringConfig.getTPatternQN());
    }
}
