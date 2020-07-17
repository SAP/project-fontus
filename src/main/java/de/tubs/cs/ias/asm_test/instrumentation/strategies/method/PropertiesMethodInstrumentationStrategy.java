package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.PropertiesStrategy;
import org.objectweb.asm.MethodVisitor;

import java.util.Properties;

public class PropertiesMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public PropertiesMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig stringConfig) {
        super(mv, stringConfig.getTPropertiesDesc(), stringConfig.getTPropertiesQN(), Constants.TPropertiesToPropertiesName, Properties.class, stringConfig, new PropertiesStrategy(stringConfig));
    }
}
