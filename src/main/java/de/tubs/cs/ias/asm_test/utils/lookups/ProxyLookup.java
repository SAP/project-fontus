package de.tubs.cs.ias.asm_test.utils.lookups;

import de.tubs.cs.ias.asm_test.taintaware.shared.IASProxyProxy;

public class ProxyLookup {
    public static boolean isProxyClass(String internalName, byte[] bytes) {
        return IASProxyProxy.isProxyClass(internalName, bytes);
    }

    public static boolean isProxyClass(Class<?> cls) {
        return IASProxyProxy.isProxyClass(cls);
    }
}
