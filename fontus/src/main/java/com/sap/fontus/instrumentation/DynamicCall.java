package com.sap.fontus.instrumentation;

import org.objectweb.asm.Handle;

import java.util.Arrays;
import java.util.Objects;

public class DynamicCall {
    public final Handle original;
    public final Handle proxy;
    private final Object[] bootstrapArguments;

    public DynamicCall(Handle original, Handle proxy, Object[] bootstrapArguments) {
        this.original = original;
        this.proxy = proxy;
        this.bootstrapArguments = bootstrapArguments;
    }

    public Handle getOriginal() {
        return this.original;
    }

    public Handle getProxy() {
        return this.proxy;
    }

    public Object[] getBootstrapArguments() {
        return this.bootstrapArguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DynamicCall)) {
            return false;
        }
        DynamicCall that = (DynamicCall) o;
        return Objects.equals(this.original, that.original) && Objects.equals(this.proxy, that.proxy) && Arrays.equals(this.bootstrapArguments, that.bootstrapArguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.original, this.proxy);
        result = 31 * result + Arrays.hashCode(this.bootstrapArguments);
        return result;
    }

}
