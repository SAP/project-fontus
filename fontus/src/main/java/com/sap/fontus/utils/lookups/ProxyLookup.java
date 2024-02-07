package com.sap.fontus.utils.lookups;

import com.sap.fontus.taintaware.unified.IASProxyProxy;

public final class ProxyLookup {
    private ProxyLookup() {
    }

    public static boolean isProxyClass(String internalName, byte[] bytes) {
        return IASProxyProxy.isProxyClass(internalName, bytes);
    }

    public static boolean isProxyClass(Class<?> cls) {
        return IASProxyProxy.isProxyClass(cls);
    }
}
