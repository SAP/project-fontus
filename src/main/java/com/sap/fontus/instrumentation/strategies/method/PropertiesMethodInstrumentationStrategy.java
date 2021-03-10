package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.PropertiesStrategy;
import org.objectweb.asm.MethodVisitor;

import java.util.Properties;

public class PropertiesMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy {
    public PropertiesMethodInstrumentationStrategy(MethodVisitor mv, TaintStringConfig stringConfig) {
        super(mv, stringConfig.getTPropertiesDesc(), stringConfig.getTPropertiesQN(), Constants.TPropertiesToPropertiesName, Properties.class, stringConfig, new PropertiesStrategy(stringConfig));
    }
}
