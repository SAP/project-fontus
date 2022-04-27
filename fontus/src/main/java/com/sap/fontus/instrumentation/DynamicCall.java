package com.sap.fontus.instrumentation;

import org.objectweb.asm.Handle;

import java.util.Arrays;
import java.util.Objects;

public class DynamicCall {
    public final Handle original;
    public final Handle proxy;
    public final Object[] bootstrapArguments;

    public DynamicCall(Handle original, Handle proxy, Object[] bootstrapArguments) {
        this.original = original;
        this.proxy = proxy;
        this.bootstrapArguments = bootstrapArguments;
    }

    public Handle getOriginal() {
        return original;
    }

    public Handle getProxy() {
        return proxy;
    }

    public Object[] getBootstrapArguments() {
        return bootstrapArguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicCall)) return false;
        DynamicCall that = (DynamicCall) o;
        return Objects.equals(original, that.original) && Objects.equals(proxy, that.proxy) && Arrays.equals(bootstrapArguments, that.bootstrapArguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(original, proxy);
        result = 31 * result + Arrays.hashCode(bootstrapArguments);
        return result;
    }

}
