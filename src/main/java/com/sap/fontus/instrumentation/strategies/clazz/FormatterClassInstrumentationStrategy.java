package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

public class FormatterClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public FormatterClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig configuration) {
        super(visitor, Constants.FormatterDesc, configuration.getTFormatterDesc(), Constants.FormatterQN, Constants.TFormatterToFormatterName);
    }
}
