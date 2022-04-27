package com.sap.fontus.taintaware.unified;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IASStringUtilsTest {

    @BeforeAll
    public static void setup() {
        Configuration.setTestConfig(TaintMethod.defaultTaintMethod());
    }

    @Test
    public void testConvertObjectString() {
        String s = "test";
        Object o = IASStringUtils.convertObject(s);
        assertTrue(o instanceof IASString);
    }

    @Test
    public void testConvertObjectIASString() {
        String s = "test";
        Object o = IASStringUtils.convertObject(IASString.fromString(s));
        assertTrue(o instanceof IASString);
    }

    @Test
    public void testConvertObjectOther() {
        List<Integer> l = new ArrayList<>();
        Object o = IASStringUtils.convertObject(l);
        assertTrue(o instanceof List);
    }

    @Test
    public void testConvertTObjectString() {
        String s = "test";
        Object o = IASStringUtils.convertTObject(s);
        assertTrue(o instanceof String);
    }

    @Test
    public void testConvertTObjectIASString() {
        String s = "test";
        Object o = IASStringUtils.convertTObject(IASString.fromString(s));
        assertTrue(o instanceof String);
    }

    @Test
    public void testConvertTObjectOther() {
        List<Integer> l = new ArrayList<>();
        Object o = IASStringUtils.convertTObject(l);
        assertTrue(o instanceof List);
    }
}
