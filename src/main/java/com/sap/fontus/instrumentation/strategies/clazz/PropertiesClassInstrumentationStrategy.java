package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
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
