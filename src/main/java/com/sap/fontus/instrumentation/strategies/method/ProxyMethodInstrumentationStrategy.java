package com.sap.fontus.instrumentation.strategies.method;

import com.sap.fontus.config.TaintStringConfig;
import com.sap.fontus.instrumentation.strategies.ProxyInstrumentation;
import com.sap.fontus.taintaware.shared.IASProxyProxy;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Proxy;

public class ProxyMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy{
    public ProxyMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig taintStringConfig) {
        super(parentVisitor, IASProxyProxy.class, Proxy.class, "getProxy", taintStringConfig, new ProxyInstrumentation());
    }
}
