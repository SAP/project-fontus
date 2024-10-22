package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.UnsafeUtils;
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
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
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

    public static void setAccessible(IASAccessibleObject<?>[] array, boolean flag) {
        for (IASAccessibleObject<?> object : array) {
            UnsafeUtils.setAccessible(object.original);
            //object.setAccessible(flag);
        }
    }

    public void setAccessible(boolean flag) {
        UnsafeUtils.setAccessible(this.original);
    }

    @SuppressWarnings("deprecation")
    public boolean isAccessible() {
        return this.original.isAccessible();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return this.original.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return this.original.getAnnotationsByType(annotationClass);
    }

    @Override
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return this.original.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return this.original.getDeclaredAnnotationsByType(annotationClass);
    }

    public IASString toIASString() {
        return IASString.valueOfInternal(this.toString());
    }

    public AccessibleObject getAccessibleObject() {
        return this.original;
    }

    @ForceInline
    public final boolean canAccess(Object obj) {
        return this.original.canAccess(obj);
    }
}
