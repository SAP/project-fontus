package com.sap.fontus.taintaware.array;

import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.TaintMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringTest {

    @BeforeAll
    public static void init() {
        Configuration.setTestConfig(TaintMethod.ARRAY);
    }

    @Test
    public void constructorTest1() {
        String s = "Hello World";

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertFalse(ts.isTainted());
    }

    @Test
    public void constructorTest2() {
        String s = "Hello World";

        IASString ts = IASString.fromString(s);

        assertEquals("Hello World", ts.getString());
        assertFalse(ts.isTainted());
    }

    @Test
    public void constructorTest3() {
        IASString s = new IASString("Hello World");
        assertTrue(s.isUninitialized());

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertTrue(ts.isUninitialized());
        assertFalse(ts.isTainted());
    }

    @Test
    public void constructorTest4() {
        IASStringBuilder s = new IASStringBuilder("Hello World");
        assertTrue(s.isUninitialized());

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertTrue(ts.isUninitialized());
        assertFalse(ts.isTainted());
    }

    @Test
    public void constructorTest5() {
        IASStringBuffer s = new IASStringBuffer("Hello World");
        assertTrue(s.isUninitialized());

        IASString ts = new IASString(s);

        assertEquals("Hello World", ts.getString());
        assertTrue(ts.isUninitialized());
        assertFalse(ts.isTainted());
    }

    @Test
    // Assumption: empty strings have always a negative taint
    public void testReplaceFirst_15() {
        IASString s1 = new IASString(new IASString("hello"));

        s1.setTaint(true);

        IASString s = s1.replaceFirst(new IASString("hello"), new IASString(""));

        assertEquals("hello", s1.toString());
        assertEquals("", s.toString());
        assertTrue(s1.isTainted());
        assertFalse(s.isTainted());
    }
}
