package com.sap.fontus.utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

public class GenericArrayTypeImpl implements GenericArrayType {
    private final Type genericComponentType;

    public GenericArrayTypeImpl(Type genericComponentType) {
        this.genericComponentType = genericComponentType;
    }

    @Override
    public Type getGenericComponentType() {
        return this.genericComponentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        GenericArrayTypeImpl that = (GenericArrayTypeImpl) o;
        return Objects.equals(this.genericComponentType, that.genericComponentType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.genericComponentType);
    }

    @Override
    public String toString() {
        return this.genericComponentType.toString();
    }
}
