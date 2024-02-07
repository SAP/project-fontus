package com.sap.fontus.utils;

import java.lang.invoke.*;

public final class LambdaMetafactory {
    private LambdaMetafactory() {
    }

    public static CallSite metafactory(
            MethodHandles.Lookup caller,
            String invokedName,
            MethodType invokedType,
            MethodType samMethodType,
            MethodHandle implMethod,
            MethodType instantiatedMethodType) throws LambdaConversionException {

        return java.lang.invoke.LambdaMetafactory.metafactory(caller, invokedName, invokedType, samMethodType, implMethod, instantiatedMethodType);
    }

    public static CallSite altMetafactory(MethodHandles.Lookup caller, String invokedName, MethodType invokedType, Object... args) throws LambdaConversionException {
        return java.lang.invoke.LambdaMetafactory.altMetafactory(caller, invokedName, invokedType, args);
    }
}
