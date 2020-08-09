package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.StringBuilderInstrumentation;
import org.objectweb.asm.MethodVisitor;

public class StringBuilderMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public StringBuilderMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(mv, configuration.getTStringBuilderDesc(), configuration.getTStringBuilderQN(), Constants.TStringBuilderToStringBuilderName, StringBuilder.class, configuration, new StringBuilderInstrumentation(configuration));
    }
}
