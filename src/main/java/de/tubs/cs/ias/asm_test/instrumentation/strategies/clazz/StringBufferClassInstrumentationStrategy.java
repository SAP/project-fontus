package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.TriConsumer;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.StringBufferInstrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.util.Optional;
import java.util.regex.Matcher;

public class StringBufferClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public StringBufferClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig config) {
        super(visitor, Constants.StringBufferDesc, config.getTStringBufferDesc(), Constants.StringBufferQN, config.getTStringBufferQN(), Constants.TStringBufferToStringBufferName);
    }
}
