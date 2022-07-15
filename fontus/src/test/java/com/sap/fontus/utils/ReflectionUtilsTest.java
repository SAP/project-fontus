package com.sap.fontus.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertSame;

class ReflectionUtilsTest {
    @Test
    void testGetCallerClass() {
        Class<?> caller = getCallerClass();

        assertSame(ReflectionUtilsTest.class, caller);
    }

    @Test
    void testGetCallerClassReflection() throws Exception {
        Method m = ReflectionUtilsTest.class.getDeclaredMethod("getCallerClass");

        Class<?> caller = (Class<?>) m.invoke(this);

        assertSame(ReflectionUtilsTest.class, caller);
    }

    private static Class<?> getCallerClass() {
        return ReflectionUtils.getCallerClass();
    }
}
