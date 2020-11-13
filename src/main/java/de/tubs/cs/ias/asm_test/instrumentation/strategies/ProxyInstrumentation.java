package de.tubs.cs.ias.asm_test.instrumentation.strategies;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASProxyProxy;

import java.lang.reflect.Proxy;

public class ProxyInstrumentation extends AbstractInstrumentation {
    public ProxyInstrumentation() {
        super(Proxy.class, IASProxyProxy.class, "getProxy");
    }
}
