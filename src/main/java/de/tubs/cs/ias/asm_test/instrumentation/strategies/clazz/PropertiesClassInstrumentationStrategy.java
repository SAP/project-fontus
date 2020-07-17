package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.Constants;
import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import org.objectweb.asm.ClassVisitor;

import java.util.Properties;

public class PropertiesClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy {
    private final String taintedQN;

    public PropertiesClassInstrumentationStrategy(ClassVisitor visitor, TaintStringConfig stringConfig) {
        super(visitor, Properties.class, stringConfig.getTPropertiesDesc(), stringConfig.getTPropertiesQN(), Constants.TPropertiesToPropertiesName);
        this.taintedQN = stringConfig.getTPropertiesQN();
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        if(superClass.equals(this.origQN)) {
            return this.taintedQN;
        }
        return superClass;
    }
}
