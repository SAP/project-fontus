package com.sap.fontus.utils;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.reflect.IASMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConversionUtilsTest {

    @BeforeAll
    public static void setup() {
        Configuration.setTestConfig(TaintMethod.defaultTaintMethod());
    }

    @Test
    public void testConvertMethodToTainted() throws NoSuchMethodException {
        Method m = Class.class.getMethod("forName", String.class);

        Object converted = ConversionUtils.convertToConcrete(m);

        assertTrue(converted instanceof IASMethod);
        assertEquals(((IASMethod) converted).getName().getString(), "forName");
    }
}
