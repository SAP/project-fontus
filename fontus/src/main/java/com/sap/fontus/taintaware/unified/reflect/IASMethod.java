package com.sap.fontus.taintaware.unified.reflect;


import com.sap.fontus.Constants;
import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.SinkParameter;
import com.sap.fontus.config.Source;
import com.sap.fontus.instrumentation.InstrumentationHelper;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASStringUtils;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
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
    private static final InstrumentationHelper helper = new InstrumentationHelper();
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
    public Object invoke(Object instance, Object... parameters) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        Object returnObj = null;

        // Handle Annotations, will always return
        if (this.original.getDeclaringClass().isAnnotation()) {
            if (this.original.getReturnType().isAssignableFrom(String.class)) {
                String result = (String) this.original.invoke(instance, parameters);
                return IASString.fromString(result);
            } else if (this.original.getReturnType().isArray() && this.original.getReturnType().getComponentType().isAssignableFrom(String.class)) {
                String[] result = (String[]) this.original.invoke(instance, parameters);
                return IASStringUtils.convertStringArray(result);
            }  else if (this.original.getReturnType().equals(Class.class)) {
                Class<?> result = (Class) this.original.invoke(instance, parameters);
                return ConversionUtils.convertClassToConcrete(result);
            }
        }

        // Check for sinks
        FunctionCall fc = FunctionCall.fromMethod(this.original);
        // We need to uninstrument the function call to ensure a match with the configuration
        FunctionCall uninstrumented = new FunctionCall(fc.getOpcode(), fc.getOwner(), fc.getName(), helper.uninstrumentForJdkCall(fc.getDescriptor()), fc.isInterface());

        Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFunction(uninstrumented);
        Method taintCheckerMethod = null;
        if (sink != null) {
            // Check for custom taint checker method
            FunctionCall taintChecker = sink.getTaintHandler();
            if (!taintChecker.isEmpty()) {
                try {
                    taintCheckerMethod = FunctionCall.toMethod(taintChecker);
                } catch (Exception e) {
                    System.err.println("Exception finding sink: " + taintChecker);
                    e.printStackTrace();
                }
            }
            // Manipulate all necessary parameters by applying taint checker
            for (SinkParameter parameter : sink.getParameters()) {
                int i = parameter.getIndex();
                if ((i > 0) && (i < parameters.length)) {
                    // Call the taint handler by reflection
                    if (taintCheckerMethod != null) {
                        parameters[i] = taintCheckerMethod.invoke(null, parameters[i], instance, sink.getFunction().getFqn(), sink.getName());
                    } else {
                        parameters[i] = IASTaintHandler.checkTaint(parameters[i], instance, sink.getFunction().getFqn(), sink.getName());
                    }
                }
            }
        }

        // Check for JDK classes
        if (lookup.isPackageExcludedOrJdk(Utils.getInternalName(this.original.getDeclaringClass())) || this.isWrapperForUninstrumentedMethod()) {
            Object[] converted = this.convertParametersToOriginal(parameters);

            if (this.original.equals(forNameMethod)) {
                Class<?> caller = ReflectionUtils.getCallerClass();
                ClassLoader callerLoader = caller.getClassLoader();
                returnObj = Class.forName((String) converted[0], true, callerLoader);
            } else {
                Object result = this.original.invoke(instance, converted);
                returnObj = ConversionUtils.convertToInstrumented(result);
            }
        } else {
            if ((!Modifier.isPublic(this.original.getModifiers()) && !Modifier.isProtected(this.original.getModifiers()) && !Modifier.isPrivate(this.original.getModifiers()))
                    || (!Modifier.isPublic(this.original.getDeclaringClass().getModifiers()) && !Modifier.isProtected(this.original.getDeclaringClass().getModifiers()) && !Modifier.isPrivate(this.original.getDeclaringClass().getModifiers()))) {
                // This method is package private. Iuff the declaring class is in the same package as the calling class we must set it accessible
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
            returnObj = this.original.invoke(instance, parameters);
        }

        // Check for sinks
        if (sink != null) {
            // Manipulate all necessary parameters by applying taint checker
            for (SinkParameter parameter : sink.getParameters()) {
                int i = parameter.getIndex();
                // Indicated return type should be transformed
                if (i == -1) {
                    // Call the taint handler by reflection
                    if (taintCheckerMethod != null) {
                        returnObj = taintCheckerMethod.invoke(null, returnObj, instance, sink.getFunction().getFqn(), sink.getName());
                    } else {
                        returnObj = IASTaintHandler.checkTaint(returnObj, instance, sink.getFunction().getFqn(), sink.getName());
                    }
                }
            }
        }

        // Check for sources
        Source source = Configuration.getConfiguration().getSourceConfig().getSourceForFunction(uninstrumented);
        Method taintHandlerMethod = null;
        if (source != null) {
            // Check for custom taint checker method
            FunctionCall taintHandler = source.getTaintHandler();
            if (!taintHandler.isEmpty()) {
                try {
                    taintHandlerMethod = FunctionCall.toMethod(taintHandler);
                } catch (Exception e) {
                    System.err.println("Exception finding source: " + taintHandler);
                    e.printStackTrace();
                }
            }
            // TODO: Why are we we checking on taintCheckerMethod here and invoking taintHandlerMethod, seems like a bug?
            if (taintCheckerMethod != null) {
                returnObj = taintHandlerMethod.invoke(null, returnObj, instance, parameters,
                        IASTaintSourceRegistry.getInstance().get(source.getName()).getId());
            } else {
                returnObj = IASTaintHandler.taint(returnObj, instance, parameters,
                        IASTaintSourceRegistry.getInstance().get(source.getName()).getId());
            }
        }

        return returnObj;
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
        return ConversionUtils.convertToInstrumented(this.original.getDefaultValue());
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
