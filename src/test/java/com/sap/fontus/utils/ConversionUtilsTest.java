package com.sap.fontus.utils;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.reflect.IASField;
import com.sap.fontus.taintaware.unified.reflect.IASMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;
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

        Object converted = ConversionUtils.convertToInstrumented(m);

        assertTrue(converted instanceof IASMethod);
        assertEquals(((IASMethod) converted).getName().getString(), "forName");
    }

    @Test
    public void testConvertFieldArrayToTainted() throws NoSuchMethodException {
        class FieldTest {
            String f1;
            String f2;
        }

        Field[] fs = FieldTest.class.getDeclaredFields();

        Object converted = ConversionUtils.convertToInstrumented(fs);

        assertTrue(converted instanceof IASField[]);
        assertEquals("f1", ((IASField[]) converted)[0].getName().getString());
        assertEquals("f2", ((IASField[]) converted)[1].getName().getString());
    }
}
