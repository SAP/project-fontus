package com.sap.fontus.gdpr.servlet;

import com.sap.fontus.taintaware.unified.IASString;

import javax.servlet.http.Cookie;

public class ReflectedCookie extends ReflectedObject {

    public static ReflectedCookie[] reflectedArray(Object o) {
        if (o == null) {
            return null;
        }
        Object[] a = (Object[]) o;
        ReflectedCookie[] cookieArray = new ReflectedCookie[a.length];
        for (int i = 0; i < a.length; i++) {
            cookieArray[i] = new ReflectedCookie(a[i]);
        }
        return cookieArray;
    }

    public ReflectedCookie(Object o) {
        super(o);
    }

    public void setComment(IASString purpose) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), purpose);
    }

    public IASString getComment() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public void setDomain(String pattern) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), pattern);
    }

    public IASString getDomain() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public void setMaxAge(int expiry) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), expiry);
    }

    public int getMaxAge() {
        return (int) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public void setPath(IASString uri) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), uri);
    }

    public IASString getPath() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public void setSecure(boolean flag) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), flag);
    }

    public boolean getSecure() {
        return (boolean) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public IASString getName() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public void setValue(String newValue) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), newValue);
    }

    public IASString getValue() {
        return (IASString) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public int getVersion() {
        return (int) this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod());
    }

    public void setVersion(int v) {
        this.callMethodWithReflection(new Object(){}.getClass().getEnclosingMethod(), v);
    }

    @Override
    public Object clone() {
        return new ReflectedCookie(o);
    }

    @Override
    public String toString() {
        return "ReflectedCookie{" +
                "name=" + getName().toString() +
                ", value=" + getValue().toString() +
                '}';
    }
}
