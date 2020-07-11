package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class MatcherClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public MatcherClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig taintStringConfig) {
        super(visitor, Constants.MatcherDesc, taintStringConfig.getTMatcherDesc(), Constants.MatcherQN, taintStringConfig.getTMatcherQN());
    }

    @Override
    public String getGetOriginalTypeMethod() {
        return Constants.TMatcherToMatcherName;
    }
}
