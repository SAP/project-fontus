package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class PatternClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public PatternClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig taintStringConfig) {
        super(visitor, Constants.PatternDesc, taintStringConfig.getTPatternDesc(), Constants.PatternQN, Constants.TPatternToPatternName);
    }
}
