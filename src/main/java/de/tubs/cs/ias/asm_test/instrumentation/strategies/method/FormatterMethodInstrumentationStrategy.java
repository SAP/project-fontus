package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.FormatterInstrumentation;
import org.objectweb.asm.MethodVisitor;

import java.util.Formatter;

public class FormatterMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public FormatterMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig stringConfig) {
        super(parentVisitor, stringConfig.getTFormatterDesc(), stringConfig.getTFormatterQN(), Constants.TFormatterToFormatterName, Formatter.class, stringConfig, new FormatterInstrumentation(stringConfig));
    }
}
