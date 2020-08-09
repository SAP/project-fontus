package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.StringBufferInstrumentation;
import org.objectweb.asm.MethodVisitor;


public class StringBufferMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public StringBufferMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(mv, configuration.getTStringBufferDesc(), configuration.getTStringBufferQN(), Constants.TStringBufferToStringBufferName, StringBuffer.class, configuration, new StringBufferInstrumentation(configuration));
    }
}
