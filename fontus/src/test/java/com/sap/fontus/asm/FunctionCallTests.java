package com.sap.fontus.asm;

import com.sap.fontus.taintaware.unified.IASTaintHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;


class FunctionCallTests {

    @Test
    void testGetMethodFromFunctionCall() throws Exception {
        Method m = String.class.getMethod("toString");
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    void testGetMethodFromFunctionCallWithArgs() throws Exception {
        Method m = String.class.getMethod("startsWith", String.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    void testGetMethodFromFunctionCallWithPrimitive() throws Exception {
        Method m = String.class.getMethod("charAt", int.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }


    @Test
    void testGetMethodFromFunctionCallWithArray() throws Exception {
        Method m = String.class.getMethod("getChars", int.class, int.class, char[].class, int.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    void testGetMethodFromTaintHandler() throws Exception {
        Method m = IASTaintHandler.class.getMethod("taint", Object.class, Object.class, Object[].class, int.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

    @Test
    void testGetMethodFromTaintChecker() throws Exception {
        Method m = IASTaintHandler.class.getMethod("checkTaint", Object.class, Object.class, String.class, String.class);
        FunctionCall fc = FunctionCall.fromMethod(m);
        Method m2 = FunctionCall.toMethod(fc);
        assertEquals(m, m2);
    }

}
