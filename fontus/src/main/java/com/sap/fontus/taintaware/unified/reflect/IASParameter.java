package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.ConversionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

public class IASParameter implements AnnotatedElement {
    private final Parameter parameter;

    public IASParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public boolean equals(Object obj) {
        return this.parameter.equals(obj);
    }

    public int hashCode() {
        return this.parameter.hashCode();
    }

    public boolean isNamePresent() {
        return this.parameter.isNamePresent();
    }

    public String toString() {
        return this.parameter.toString();
    }

    public IASString toIASString() {
        return new IASString(this.parameter.toString());
    }

    public IASExecutable<?> getDeclaringExecutable() {
        return IASReflectRegistry.getInstance().mapExecutable(this.parameter.getDeclaringExecutable());
    }

    public int getModifiers() {
        return this.parameter.getModifiers();
    }

    public IASString getName() {
        return IASString.valueOf(this.parameter.getName());
    }

    public Type getParameterizedType() {
        return ConversionUtils.convertTypeToInstrumented(this.parameter.getParameterizedType());
    }

    public Class<?> getType() {
        return ConversionUtils.convertClassToConcrete(this.parameter.getType());
    }

    public AnnotatedType getAnnotatedType() {
        return this.parameter.getAnnotatedType();
    }

    public boolean isImplicit() {
        return this.parameter.isImplicit();
    }

    public boolean isSynthetic() {
        return this.parameter.isSynthetic();
    }

    public boolean isVarArgs() {
        return this.parameter.isVarArgs();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.parameter.getAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return this.parameter.getAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return this.parameter.getDeclaredAnnotations();
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return this.parameter.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return this.parameter.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return this.parameter.getAnnotations();
    }

    public Parameter getParameter() {
        return this.parameter;
    }
}
