package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.utils.ConversionUtils;

import java.lang.reflect.Method;

public class IASReflectionProxy {
    public static Object handleInvocationProxyCall(Object result, Object proxy, Method method, Object[] args) {
        if (method.getReturnType().equals(String.class) || method.getReturnType().equals(String[].class)) {
            if (result instanceof IASString) {
                return ((IASString) result).getString();
            } else if (result.getClass() == IASString[].class) {
                return ConversionUtils.convertToUninstrumented(result);
            }
        }
        return result;
    }
}
