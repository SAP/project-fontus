package de.tubs.cs.ias.asm_test;

import java.util.Objects;

public final class FieldData {
    private final String name;
    private final String descriptor;
    private final Object value;

    private FieldData(String name, String descriptor, Object value) {
        this.name = name;
        this.descriptor = descriptor;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public Object getValue() {
        return this.value;
    }

    public static FieldData of(String name, String descriptor, Object value) {
        return new FieldData(name, descriptor, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        FieldData fieldData = (FieldData) obj;
        return Objects.equals(this.name, fieldData.name) &&
                Objects.equals(this.descriptor, fieldData.descriptor) &&
                Objects.equals(this.value, fieldData.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.descriptor, this.value);
    }
}
