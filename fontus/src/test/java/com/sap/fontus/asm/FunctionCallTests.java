package com.sap.fontus.asm;

import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FunctionCallTests {

    @Test
    public void testGetMethodFromFunctionCall() throws NoSuchMethodException, ClassNotFoundException {
        Method m = String.class.getMethod("toString");
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    public void testGetMethodFromFunctionCallWithArgs() throws NoSuchMethodException, ClassNotFoundException {
        Method m = String.class.getMethod("startsWith", String.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    public void testGetMethodFromFunctionCallWithPrimitive() throws NoSuchMethodException, ClassNotFoundException {
        Method m = String.class.getMethod("charAt", int.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }


    @Test
    public void testGetMethodFromFunctionCallWithArray() throws NoSuchMethodException, ClassNotFoundException {
        Method m = String.class.getMethod("getChars", int.class, int.class, char[].class, int.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    public void testGetMethodFromTaintHandler() throws NoSuchMethodException, ClassNotFoundException {
        Method m = IASTaintHandler.class.getMethod("taint", Object.class, Object.class, Object[].class, int.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    public void testGetMethodFromTaintChecker() throws NoSuchMethodException, ClassNotFoundException {
        Method m = IASTaintHandler.class.getMethod("checkTaint", Object.class, Object.class, String.class, String.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

}
