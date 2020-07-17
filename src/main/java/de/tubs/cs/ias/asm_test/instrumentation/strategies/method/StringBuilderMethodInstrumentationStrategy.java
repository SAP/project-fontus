package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.StringBuilderInstrumentation;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.util.HashMap;
import java.util.regex.Matcher;

public class StringBuilderMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public StringBuilderMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig configuration) {
        super(mv, configuration.getTStringBuilderDesc(), configuration.getTStringBuilderQN(), Constants.TStringBuilderToStringBuilderName, StringBuilder.class, configuration, new StringBuilderInstrumentation(configuration));
    }
}
