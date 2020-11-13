package de.tubs.cs.ias.asm_test.instrumentation.strategies.clazz;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASProxyProxy;
import org.objectweb.asm.ClassVisitor;

import java.lang.reflect.Proxy;

public class ProxyClassInstrumentationStrategy extends AbstractClassInstrumentationStrategy{
    public ProxyClassInstrumentationStrategy(ClassVisitor visitor) {
        super(visitor, Proxy.class, IASProxyProxy.class, "getProxy");
    }
}
