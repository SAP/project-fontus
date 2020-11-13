package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class StringBuilderClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public StringBuilderClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig config) {
        super(visitor, Constants.StringBuilderDesc, config.getTStringBuilderDesc(), Constants.StringBuilderQN, Constants.TStringBuilderToStringBuilderName);
    }
}