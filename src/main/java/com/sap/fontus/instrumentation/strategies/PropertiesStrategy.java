package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import org.objectweb.asm.Type;

import java.util.Properties;

public class PropertiesStrategy extends AbstractInstrumentation {
    public PropertiesStrategy(TaintStringConfig taintStringConfig, InstrumentationHelper instrumentationHelper) {
        super(Type.getType(Properties.class), Type.getObjectType(taintStringConfig.getTPropertiesQN()), instrumentationHelper, Constants.TPropertiesToPropertiesName);
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        if(superClass.equals(this.origType.getInternalName())) {
            return this.instrumentedType.getInternalName();
        }
        return superClass;
    }
}
