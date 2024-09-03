package com.sap.fontus.taintaware.unified.reflect;

import com.sap.fontus.taintaware.unified.IASString;

import java.lang.reflect.Member;


public interface IASMember {
    Class<?> getDeclaringClass();

    IASString getName();

    int getModifiers();

    boolean isSynthetic();

    Member getMember();
}
