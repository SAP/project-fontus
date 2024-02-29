package com.sap.fontus.gdpr.servlet;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectedObjectTests {
    @BeforeAll
    static void init() {
        Configuration.setTestConfig(TaintMethod.RANGE);
    }

    @Test
    public void testCallWithReflection() {
        Widget w = new Widget("foobar");
        ReflectedWidget rw = new ReflectedWidget(w);
        assertEquals("foobar", rw.getName());
    }

    @Test
    public void testStaticCallWithReflection() {
        assertEquals("Widget", ReflectedWidget.getClassName());
        ReflectedWidget.setClassName("foobar");
        assertEquals("foobar", ReflectedWidget.getClassName());

    }

}
