package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.Constants;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.shared.IASProxyProxy;
import org.objectweb.asm.Type;

import java.lang.reflect.Proxy;

public class ProxyInstrumentation extends AbstractInstrumentation {
    public ProxyInstrumentation(InstrumentationHelper instrumentationHelper) {
        super(Type.getType(Proxy.class), Type.getType(IASProxyProxy.class), instrumentationHelper, Constants.TProxyToProxyName);
    }
}
