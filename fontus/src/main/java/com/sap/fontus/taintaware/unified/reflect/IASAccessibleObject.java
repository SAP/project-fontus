package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;

public abstract class IASAccessibleObject<T extends AccessibleObject> implements AnnotatedElement {
    protected final T original;

    protected IASAccessibleObject(T original) {
        this.original = original;
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

    public static void setAccessible(IASAccessibleObject[] array, boolean flag) {
        for (IASAccessibleObject object : array) {
            object.setAccessible(flag);
        }
    }

    public void setAccessible(boolean flag) {
        this.original.setAccessible(flag);
    }

    public boolean isAccessible() {
        return this.original.isAccessible();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return this.original.isAnnotationPresent(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return this.original.getAnnotationsByType(annotationClass);
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return this.original.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return this.original.getDeclaredAnnotationsByType(annotationClass);
    }

    public IASString toIASString() {
        return IASString.valueOf(this.toString());
    }

    public AccessibleObject getAccessibleObject() {
        return this.original;
    }

    @ForceInline
    public final boolean canAccess(Object obj) {
        return this.original.canAccess(obj);
    }
}
