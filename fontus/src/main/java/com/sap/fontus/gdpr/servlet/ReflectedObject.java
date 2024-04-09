package com.sap.fontus.gdpr.servlet;

import com.sap.fontus.utils.UnsafeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ReflectedObject {

    protected final Object o;

    /**
     * Calls a static method via reflection.
     */
    public static Object callMethodWithReflection(Class<?> c, Method m, Object... args) {
        Object result = null;
        try {
            Method originalMethod = c.getMethod(m.getName(), m.getParameterTypes());
            // TODO: check whether this is a bug with the missing instance parameter.
            result = originalMethod.invoke(null, args);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            System.err.println("FONTUS Exception with reflected call: " + c.getName() + "." + m.getName() + ": " + e.getMessage());
        }
        return result;
    }

    protected Object callMethodWithReflection(Method m, Object... args) {
        Object result = null;
        try {
            Method originalMethod = this.o.getClass().getMethod(m.getName(), m.getParameterTypes());
            UnsafeUtils.setAccessible(originalMethod);
            result = originalMethod.invoke(this.o, args);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            System.err.println("FONTUS Exception with reflected call: " + this.o.getClass().getName() + "." + m.getName() + ": " + e.getMessage());
        }
        return result;
    }

    ReflectedObject(Object o) {
        this.o = o;
    }

}
