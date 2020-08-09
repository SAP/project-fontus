package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.PatternInstrumentation;
import org.objectweb.asm.MethodVisitor;

import java.util.regex.Pattern;

public class PatternMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public PatternMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig taintStringConfig) {
        super(parentVisitor, taintStringConfig.getTPatternDesc(), taintStringConfig.getTPatternQN(), Constants.TPatternToPatternName, Pattern.class, taintStringConfig, new PatternInstrumentation(taintStringConfig));
    }
}
