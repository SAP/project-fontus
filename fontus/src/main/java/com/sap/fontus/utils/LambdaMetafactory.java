package com.sap.fontus.utils;

import com.sap.fontus.taintaware.unified.IASLookupUtils;

import java.lang.invoke.*;

public class LambdaMetafactory {
    public static CallSite metafactory(
            MethodHandles.Lookup caller,
            String invokedName,
            MethodType invokedType,
            MethodType samMethodType,
            MethodHandle implMethod,
            MethodType instantiatedMethodType) throws LambdaConversionException {

        CallSite cs = java.lang.invoke.LambdaMetafactory.metafactory(caller, invokedName, invokedType, samMethodType, implMethod, instantiatedMethodType);

        return cs;
    }

    public static CallSite altMetafactory(MethodHandles.Lookup caller, String invokedName, MethodType invokedType, Object... args) throws LambdaConversionException {
        return java.lang.invoke.LambdaMetafactory.altMetafactory(caller, invokedName, invokedType, args);
    }
}
