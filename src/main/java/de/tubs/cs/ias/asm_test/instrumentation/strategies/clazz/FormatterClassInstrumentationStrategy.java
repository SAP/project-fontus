package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class FormatterClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public FormatterClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig configuration) {
        super(visitor, Constants.FormatterDesc, configuration.getTFormatterDesc(), Constants.FormatterQN, Constants.TFormatterToFormatterName);
    }
}
