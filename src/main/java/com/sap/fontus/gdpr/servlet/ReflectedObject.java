package com.sap.fontus.gdpr.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ReflectedObject {

    protected Object o;

    protected Object callMethodWithReflection(Method m, Object... args) {
        Object result = null;
        try {
            Method original_method = o.getClass().getMethod(m.getName(), m.getParameterTypes());
            result = original_method.invoke(o, args);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected ReflectedObject(Object o) {
        this.o = o;
    }

}
