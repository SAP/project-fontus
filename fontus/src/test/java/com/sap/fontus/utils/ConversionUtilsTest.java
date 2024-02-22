package com.sap.fontus.utils;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.reflect.IASField;
import com.sap.fontus.taintaware.unified.reflect.IASMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversionUtilsTest {

    @BeforeAll
    static void setup() {
        Configuration.setTestConfig(TaintMethod.defaultTaintMethod());
    }

    @Test
    void testConvertMethodToTainted() throws NoSuchMethodException {
        Method m = Class.class.getMethod("forName", String.class);

        Object converted = ConversionUtils.convertToInstrumented(m);

        assertTrue(converted instanceof IASMethod);
        assertEquals(((IASMethod) converted).getName().getString(), "forName");
    }

    @Test
    void testConvertFieldArrayToTainted() {
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

    @Test
    void test2dArrays() {
        Locale lc = Locale.US;
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(lc);
        String[][] zs = dfs.getZoneStrings();
        Object o = ConversionUtils.convertToInstrumented(zs);
        IASString[][] czs = (IASString[][]) o;
        assertEquals(czs.length, zs.length);
    }
}
