package de.tubs.cs.ias.asm_test.instrumentation.strategies.method;

import de.tubs.cs.ias.asm_test.config.TaintStringConfig;
import de.tubs.cs.ias.asm_test.instrumentation.strategies.ProxyInstrumentation;
import de.tubs.cs.ias.asm_test.taintaware.shared.IASProxyProxy;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Proxy;

public class ProxyMethodInstrumentationStrategy extends AbstractMethodInstrumentationStrategy{
    public ProxyMethodInstrumentationStrategy(MethodVisitor parentVisitor, TaintStringConfig taintStringConfig) {
        super(parentVisitor, IASProxyProxy.class, Proxy.class, "getProxy", taintStringConfig, new ProxyInstrumentation());
    }
}
