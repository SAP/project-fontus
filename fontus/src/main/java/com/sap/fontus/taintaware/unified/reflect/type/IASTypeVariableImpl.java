package com.sap.fontus.taintaware.unified.reflect.type;

import com.sap.fontus.utils.ConversionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

public class IASTypeVariableImpl<T extends GenericDeclaration> implements TypeVariable<T> {
    private final TypeVariable<T> original;

    public IASTypeVariableImpl(TypeVariable<T> original) {
        this.original = original;
    }

    public TypeVariable<T> getType() {
        return this.original;
    }

    @Override
    public Type[] getBounds() {
        return Arrays.stream(this.original.getBounds()).map(ConversionUtils::convertTypeToInstrumented).toArray(Type[]::new);
    }

    @Override
    public T getGenericDeclaration() {
        return this.original.getGenericDeclaration();
    }

    @Override
    public String getName() {
        return this.original.getName();
    }

    @Override
    public AnnotatedType[] getAnnotatedBounds() {
        return this.original.getAnnotatedBounds();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.original.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return this.original.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return this.original.getDeclaredAnnotations();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IASTypeVariableImpl) {
            return this.original.equals(((IASTypeVariableImpl<?>) obj).original);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.original.hashCode();
    }
}
