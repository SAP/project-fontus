package de.tubs.cs.ias.asm_test.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReflectionUtilsTest {
    @Test
    public void testGetCallerClass() {
        Class caller = getCallerClass();

        assertEquals(ReflectionUtilsTest.class, caller);
    }

    @Test
    public void testGetCallerClassReflection() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = ReflectionUtilsTest.class.getDeclaredMethod("getCallerClass");

        Class caller = (Class) m.invoke(this);

        assertEquals(ReflectionUtilsTest.class, caller);
    }

    private Class getCallerClass() {
        return ReflectionUtils.getCallerClass();
    }
}
