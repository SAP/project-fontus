package com.sap.fontus.gdpr.servlet;

import com.sap.fontus.taintaware.unified.IASString;

public class ReflectedSession extends ReflectedObject {
    public ReflectedSession(Object o) {
        super(o);
    }

    public Object getAttribute(String name) {
        if (name == null) {
            return null;
        }
        return this.getAttribute(new IASString(name));
    }

    public Object getAttribute(IASString name) {
        return this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), name);
    }
}
