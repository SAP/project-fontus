package com.sap.fontus.taintaware.unified.reflect;


import com.sap.fontus.Constants;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringUtils;
import com.sap.fontus.taintaware.unified.reflect.type.IASTypeVariableImpl;
import com.sap.fontus.utils.ConversionUtils;
import com.sap.fontus.utils.ReflectionUtils;
import com.sap.fontus.utils.Utils;
import com.sap.fontus.utils.lookups.CombinedExcludedLookup;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.vm.annotation.ForceInline;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;

public class IASMethod extends IASExecutable<Method> {
    private static final CombinedExcludedLookup lookup = new CombinedExcludedLookup();
    private static final Method forNameMethod;

    static {
        try {
            forNameMethod = Class.class.getMethod("forName", String.class);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not load method Class.forName");
            throw new RuntimeException(e);
        }
    }

    private Boolean isWrapperForUninstrumentedMethod;

    public IASMethod(Method method) {
        super(method);
    }

    @Override
    public void setAccessible(boolean flag) {
        super.setAccessible(flag);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return this.original.getDeclaringClass();
    }

    @Override
    public IASString getName() {
        return new IASString(this.original.getName());
    }

    @Override
    public int getModifiers() {
        return this.original.getModifiers();
    }

    @Override
    public TypeVariable<Method>[] getTypeParameters() {
        return Arrays.stream(this.original.getTypeParameters()).map(IASTypeVariableImpl::new).toArray(TypeVariable[]::new);
    }

    public Class<?> getReturnType() {
        if (this.original.getDeclaringClass().isAnnotation()) {
            if (this.original.getReturnType() == String.class) {
                return IASString.class;
            } else if (this.original.getReturnType() == String[].class) {
                return IASString[].class;
            }
        }
        return this.original.getReturnType();
    }

    public Type getGenericReturnType() {
        return ConversionUtils.convertTypeToInstrumented(this.original.getGenericReturnType());
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return Arrays.stream(this.original.getParameterTypes()).map(ConversionUtils::convertClassToConcrete).toArray(Class[]::new);
    }

    @Override
    public int getParameterCount() {
        return super.getParameterCount();
    }

    @Override
    public Type[] getGenericParameterTypes() {
        return super.getGenericParameterTypes();
    }

    @Override
    public Class<?>[] getExceptionTypes() {
        return this.original.getExceptionTypes();
    }

    @Override
    public Type[] getGenericExceptionTypes() {
        return super.getGenericExceptionTypes();
    }

    private boolean isWrapperForUninstrumentedMethod() {
        if (this.isWrapperForUninstrumentedMethod == null) {
            this.isWrapperForUninstrumentedMethod = !this.getReturnType().equals(this.original.getReturnType()) || !Arrays.equals(this.getParameterTypes(), this.original.getParameterTypes());
        }
        return this.isWrapperForUninstrumentedMethod;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IASMethod) {
            return this.original.equals(((IASMethod) obj).original);
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
        return IASString.valueOf(this.original.toGenericString());
    }

    @SuppressWarnings("Since15")
    @CallerSensitive
    @ForceInline
    public Object invoke(Object instance, Object... parameters) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        if (this.original.getDeclaringClass().isAnnotation()) {
            if (this.original.getReturnType().isAssignableFrom(String.class)) {
                String result = (String) this.original.invoke(instance, parameters);
                return IASString.fromString(result);
            } else if (this.original.getReturnType().isArray() && this.original.getReturnType().getComponentType().isAssignableFrom(String.class)) {
                String[] result = (String[]) this.original.invoke(instance, parameters);
                return IASStringUtils.convertStringArray(result);
            }
        } else if (lookup.isPackageExcludedOrJdk(Utils.getInternalName(this.original.getDeclaringClass())) || isWrapperForUninstrumentedMethod()) {
            Object[] converted = convertParametersToOriginal(parameters);

            if (this.original.equals(forNameMethod)) {
                Class caller = ReflectionUtils.getCallerClass();
                ClassLoader callerLoader = caller.getClassLoader();
                return Class.forName((String) converted[0], true, callerLoader);
            }

            Object result = this.original.invoke(instance, converted);
            return ConversionUtils.convertToConcrete(result);
        }
        if ((!Modifier.isPublic(this.original.getModifiers()) && !Modifier.isProtected(this.original.getModifiers()) && !Modifier.isPrivate(this.original.getModifiers()))
                || (!Modifier.isPublic(this.original.getDeclaringClass().getModifiers()) && !Modifier.isProtected(this.original.getDeclaringClass().getModifiers()) && !Modifier.isPrivate(this.original.getDeclaringClass().getModifiers()))) {
            // This method is package private. Iuff the declaring class is in the same package as the calling class we must set it accessible
            // Otherwise the caller class (which is this class) is not in the same package as the declaring class an an IllegalAccessException is thrown
            Class callerClass;
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
        return this.original.invoke(instance, parameters);
    }

    public boolean isBridge() {
        return this.original.isBridge();
    }

    @Override
    public boolean isVarArgs() {
        return super.isVarArgs();
    }

    @Override
    public boolean isSynthetic() {
        return super.isSynthetic();
    }

    public boolean isDefault() {
        return this.original.isDefault();
    }

    public Object getDefaultValue() {
        return ConversionUtils.convertToConcrete(this.original.getDefaultValue());
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
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

    public Method getMethod() {
        return this.original;
    }
}
