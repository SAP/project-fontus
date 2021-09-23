package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.ConversionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;

public abstract class IASExecutable<T extends Executable> extends IASAccessibleObject<T> implements IASMember, GenericDeclaration {
    protected IASExecutable(T executable) {
        super(executable);
    }

    public int getParameterCount() {
        return this.original.getParameterCount();
    }

    public Type[] getGenericParameterTypes() {
        return Arrays.stream(this.original.getGenericParameterTypes()).map(ConversionUtils::convertTypeToInstrumented).toArray(Type[]::new);
    }

    public IASParameter[] getParameters() {
        return Arrays.stream(this.original.getParameters()).map(IASParameter::new).toArray(IASParameter[]::new);
    }

    public Type[] getGenericExceptionTypes() {
        return Arrays.stream(this.original.getGenericParameterTypes()).map(ConversionUtils::convertTypeToInstrumented).toArray(Type[]::new);
    }

    public boolean isVarArgs() {
        return this.original.isVarArgs();
    }

    public boolean isSynthetic() {
        return this.original.isSynthetic();
    }

    @Override
    public <R extends Annotation> R getAnnotation(Class<R> annotationClass) {
        return this.original.getAnnotation(annotationClass);
    }

    @Override
    public <R extends Annotation> R[] getAnnotationsByType(Class<R> annotationClass) {
        return this.original.getAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return this.original.getDeclaredAnnotations();
    }

    public AnnotatedType[] getAnnotatedParameterTypes() {
        return this.original.getAnnotatedParameterTypes();
    }

    public AnnotatedType[] getAnnotatedExceptionTypes() {
        return this.original.getAnnotatedExceptionTypes();
    }

    public abstract Class<?> getDeclaringClass();

    public abstract IASString getName();

    public abstract int getModifiers();

    public abstract TypeVariable<?>[] getTypeParameters();

    public abstract Class<?>[] getParameterTypes();

    public abstract Class<?>[] getExceptionTypes();

    public abstract IASString toGenericString();

    public abstract Annotation[][] getParameterAnnotations();

    public abstract AnnotatedType getAnnotatedReturnType();


    protected Object[] convertParametersToOriginal(Object[] parameters) {
        if (parameters == null) {
            return null;
        }
        Object[] converted = new Object[parameters.length];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = ConversionUtils.convertToOrig(parameters[i]);
        }
        return converted;
    }

    public Executable getExecutable() {
        return this.original;
    }

    public Member getMember() {
        return this.original;
    }
}
