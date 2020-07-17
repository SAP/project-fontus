package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.asm.Descriptor;
import de.tubs.cs.ias.asm_test.utils.Utils;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.FormatterInstrumentation;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.InstrumentationHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import de.tubs.cs.ias.asm_test.utils.ParentLogger;
import de.tubs.cs.ias.asm_test.utils.LogUtils;

import java.util.Formatter;
import java.util.HashMap;
import java.util.regex.Matcher;

public class FormatterMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public FormatterMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig stringConfig) {
        super(parentVisitor, Constants.FormatterDesc, stringConfig.getTFormatterDesc(), stringConfig.getTFormatterQN(), Formatter.class, stringConfig, new FormatterInstrumentation(stringConfig));
    }
}
