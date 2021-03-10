package com.sap.fontus.asm;

import java.util.Objects;

public class ProxiedDynamicFunctionEntry {
    private final String name;
    private final String descriptor;

    public ProxiedDynamicFunctionEntry(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        ProxiedDynamicFunctionEntry that = (ProxiedDynamicFunctionEntry) obj;
        return this.name.equals(that.name) &&
                this.descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.descriptor);
    }
}
