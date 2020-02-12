package de.tubs.cs.ias.asm_test.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.MethodVisitor;

import java.util.regex.Matcher;

public class MatcherMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {

    public MatcherMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig taintStringConfig) {
        super(parentVisitor, Constants.MatcherDesc, taintStringConfig.getTMatcherDesc(), Constants.MatcherQN, taintStringConfig.getTMatcherQN(), Constants.TMatcherToMatcherName, Matcher.class, taintStringConfig);
    }
}
