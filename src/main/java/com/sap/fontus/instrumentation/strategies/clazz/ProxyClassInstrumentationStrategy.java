package com.sap.fontus.instrumentation.strategies.clazz;

import com.sap.fontus.taintaware.shared.IASProxyProxy;
import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Proxy;

public class ProxyClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy{
    public ProxyClassInstrumentationStrategy(ClassVisitor visitor) {
        super(visitor, Proxy.class, IASProxyProxy.class, "getProxy");
    }
}
