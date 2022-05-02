package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;

import java.lang.reflect.Member;


public interface IASMember {
    public Class<?> getDeclaringClass();

    public IASString getName();

    public int getModifiers();

    public boolean isSynthetic();

    public Member getMember();
}
