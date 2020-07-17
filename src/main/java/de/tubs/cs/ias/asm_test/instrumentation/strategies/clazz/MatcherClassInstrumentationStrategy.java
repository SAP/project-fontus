package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

import java.util.regex.Matcher;

public class MatcherClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public MatcherClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig taintStringConfig) {
        super(visitor, Matcher.class, taintStringConfig.getTMatcherDesc(), taintStringConfig.getTMatcherQN(), Constants.TMatcherToMatcherName);
    }
}
