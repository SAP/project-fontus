package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class StringBufferClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public StringBufferClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig config) {
        super(visitor, Constants.StringBufferDesc, config.getTStringBufferDesc(), Constants.StringBufferQN, config.getTStringBufferQN(), Constants.TStringBufferToStringBufferName);
    }
}
