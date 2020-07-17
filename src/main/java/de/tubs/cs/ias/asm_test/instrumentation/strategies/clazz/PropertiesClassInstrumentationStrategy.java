package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

import java.util.Properties;

public class PropertiesClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    public PropertiesClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig stringConfig) {
        super(visitor, Properties.class, stringConfig.getTPropertiesDesc(), stringConfig.getTPropertiesQN(), Constants.TPropertiesToPropertiesName);
    }
}
