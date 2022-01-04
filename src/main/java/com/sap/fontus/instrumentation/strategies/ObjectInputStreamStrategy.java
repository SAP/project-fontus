package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.unified.IASObjectInputStream;
import org.objectweb.asm.Type;

import java.io.ObjectInputStream;

public class ObjectInputStreamStrategy extends AbstractInstrumentation  {
    public ObjectInputStreamStrategy(InstrumentationHelper instrumentationHelper) {
        super(Type.getType(ObjectInputStream.class), Type.getType(IASObjectInputStream.class), instrumentationHelper, Constants.TProxyToProxyName);
    }

    @Override
    public String instrumentSuperClass(String superClass) {
        if (superClass.equals(this.origType.getInternalName())) {
            return this.instrumentedType.getInternalName();
        }
        return superClass;
    }
}
