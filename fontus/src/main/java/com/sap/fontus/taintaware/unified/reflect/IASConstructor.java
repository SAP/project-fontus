package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.Constants;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.reflect.type.IASTypeVariableImpl;
import com.sap.fontus.utils.ConversionUtils;
import com.sap.fontus.utils.ReflectionUtils;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;

public class IASConstructor<T> extends IASExecutable<Constructor<T>> {
    private static final CombinedExcludedLookup lookup = new CombinedExcludedLookup();
    public IASConstructor(Constructor<T> constructor) {
        super(constructor);
    }

    @Override
    public void setAccessible(boolean flag) {
        super.setAccessible(flag);
    }

    @Override
    public Class<T> getDeclaringClass() {
        return this.original.getDeclaringClass();
    }

    @Override
    public IASString getName() {
        return IASString.fromString(this.original.getName());
    }

    @Override
    public int getModifiers() {
        return this.original.getModifiers();
    }

    @Override
    public TypeVariable<Constructor<T>>[] getTypeParameters() {
        return Arrays.stream(this.original.getTypeParameters()).map(IASTypeVariableImpl::new).toArray(IASTypeVariableImpl[]::new);
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return Arrays.stream(this.original.getParameterTypes()).map(ConversionUtils::convertClassToConcrete).toArray(Class[]::new);
    }

    @Override
    public int getParameterCount() {
        return this.original.getParameterCount();
    }

    @Override
    public Type[] getGenericParameterTypes() {
        return super.getGenericParameterTypes();
    }

    @Override
    public Class<?>[] getExceptionTypes() {
        return Arrays.stream(this.original.getExceptionTypes()).map(ConversionUtils::convertClassToConcrete).toArray(Class[]::new);
    }

    @Override
    public Type[] getGenericExceptionTypes() {
        return super.getGenericExceptionTypes();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IASConstructor<?> other) {
            if (this.getDeclaringClass() == other.getDeclaringClass()) {
                return Arrays.equals(this.getParameterTypes(), other.getParameterTypes());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.original.hashCode();
    }

    @Override
    public String toString() {
        return this.original.toString();
    }

    @Override
    public IASString toGenericString() {
        return new IASString(this.original.toGenericString());
    }

    @SuppressWarnings("Since15")
//    @CallerSensitive
//    @ForceInline
    public Object newInstance(Object... parameters) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (lookup.isPackageExcludedOrJdk(Utils.getInternalName(this.original.getDeclaringClass()))) {
            Object[] converted = this.convertParametersToOriginal(parameters);
            return this.original.newInstance(converted);
        }
        if ((!Modifier.isPublic(this.original.getModifiers()) && !Modifier.isProtected(this.original.getModifiers()) && !Modifier.isPrivate(this.original.getModifiers()))
                || (!Modifier.isPublic(this.original.getDeclaringClass().getModifiers()) && !Modifier.isProtected(this.original.getDeclaringClass().getModifiers()) && !Modifier.isPrivate(this.original.getDeclaringClass().getModifiers()))) {
            // This method is package private. If the declaring class is in the same package as the calling class we must set it accessible
            // Otherwise the caller class (which is this class) is not in the same package as the declaring class an an IllegalAccessException is thrown
            Class<?> callerClass;
            if (Constants.JAVA_VERSION >= 9) {
                callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                        .getCallerClass();
            } else {
                callerClass = ReflectionUtils.getCallerClass();
            }
            if (this.original.getDeclaringClass().getPackage().equals(callerClass.getPackage())) {
                this.original.setAccessible(true);
            }
        }
        return this.original.newInstance(parameters);
    }

    @Override
    public boolean isVarArgs() {
        return super.isVarArgs();
    }

    @Override
    public boolean isSynthetic() {
        return super.isSynthetic();
    }

    @Override
    public <R extends Annotation> R getAnnotation(Class<R> annotationClass) {
        return super.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return super.getDeclaredAnnotations();
    }

    @Override
    public Annotation[][] getParameterAnnotations() {
        return this.original.getParameterAnnotations();
    }

    @Override
    public AnnotatedType getAnnotatedReturnType() {
        return this.original.getAnnotatedReturnType();
    }

    public AnnotatedType getAnnotatedReceiverType() {
        return this.original.getAnnotatedReceiverType();
    }

    public Constructor<T> getConstructor() {
        return this.original;
    }
}
