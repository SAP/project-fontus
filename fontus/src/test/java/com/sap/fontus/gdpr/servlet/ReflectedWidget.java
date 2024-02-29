package com.sap.fontus.gdpr.servlet;

import java.lang.reflect.Method;

class ReflectedWidget extends ReflectedObject {
    ReflectedWidget(Object o) {
        super(o);
    }

    String getName() {
        Method em = new Object() {
        }.getClass().getEnclosingMethod();
        System.out.println(em);
        return (String) callMethodWithReflection(em);
    }

    public static String getClassName() {
        return (String) callMethodWithReflection(Widget.class, new Object() {
        }.getClass().getEnclosingMethod());
    }

    public static void setClassName(String s) {
        callMethodWithReflection(Widget.class, new Object() {
        }.getClass().getEnclosingMethod(), s);
    }
}
