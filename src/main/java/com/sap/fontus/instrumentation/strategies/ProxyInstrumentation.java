package com.sap.fontus.instrumentation.strategies;

import com.sap.fontus.taintaware.shared.IASProxyProxy;

import java.lang.reflect.Proxy;

public class ProxyInstrumentation extends AbstractInstrumentation {
    public ProxyInstrumentation() {
        super(Proxy.class, IASProxyProxy.class, "getProxy");
    }
}
