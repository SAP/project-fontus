package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.FormatterInstrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.util.Optional;
import java.util.regex.Matcher;

public class FormatterClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public FormatterClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig configuration) {
        super(visitor, Constants.FormatterDesc, configuration.getTFormatterDesc(), Constants.FormatterQN, configuration.getTFormatterQN(), Constants.TFormatterToFormatterName);
    }
}
