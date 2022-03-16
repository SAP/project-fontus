package com.sap.fontus.gdpr.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ReflectedObject {

    protected Object o;

    public static Object callMethodWithReflection(Class c, Method m, Object... args) {
        Object result = null;
        try {
            Method original_method = c.getMethod(m.getName(), m.getParameterTypes());
            result = original_method.invoke(args);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            System.err.println("FONTUS Exception with reflected call: " + c.getName() + "." + m.getName() + ": " + e.getMessage());
        }
        return result;
    }

    protected Object callMethodWithReflection(Method m, Object... args) {
        Object result = null;
        try {
            Method original_method = o.getClass().getMethod(m.getName(), m.getParameterTypes());
            result = original_method.invoke(o, args);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            System.err.println("FONTUS Exception with reflected call: " + o.getClass().getName() + "." + m.getName() + ": " + e.getMessage());
        }
        return result;
    }

    protected ReflectedObject(Object o) {
        this.o = o;
    }

}
